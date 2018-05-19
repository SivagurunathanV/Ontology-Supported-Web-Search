package com.sw.osws.clean;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.tdb.TDBFactory;

import java.io.*;
import java.util.*;

public class BabelNetQueryImpl {

    private static Map<String, Person> personMap = new HashMap<String, Person>();
    private static Map<String, Set<Person>> typeWisePeople = new HashMap<String, Set<Person>>();

    private static final String BASE_NAMESPACE = "http://utdallas/semclass#";

    public static void main(String[] args) throws IOException {
        org.apache.log4j.Logger.getRootLogger()
                .setLevel(org.apache.log4j.Level.OFF);

        /*BufferedReader br = new BufferedReader(new FileReader("full_data.csv"));

        String line;
        while ((line = br.readLine()) != null) {
            String[] nameParts = line.split(",");

            for (int i = 1; i < nameParts.length; i++) {
                if (nameParts[i].split(" ").length > 1)
                    continue;
                String name = WordUtils.capitalize(nameParts[0].toLowerCase() + "_" + nameParts[i], '_');

                System.out.println(name);
                createPersonForName(name);


            }
        }
*/

     //  preprocessRelationship();

       String directory = "KB" ;
        Dataset dataset = TDBFactory.createDataset(directory);

        dataset.begin(ReadWrite.WRITE);

        Model model = dataset.getNamedModel("myrdf");

        createRDFKnowledgeBase(dataset, model);

        dataset.begin(ReadWrite.READ);

        String q = "SELECT ?n ?result\n" +
                "WHERE { ?s <http://xmlns.com/foaf/0.1/name> ?n \n" +
                "FILTER regex(?n,'^m','i') . \n" +
                "?s <http://xmlns.com/foaf/0.1/depiction> ?result}";

        Query query = QueryFactory.create(q);
        QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
        ResultSet resultSet = queryExecution.execSelect();
        try {
            ResultSetFormatter.outputAsXML(new FileOutputStream(new File("out")), resultSet);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        queryExecution.close();


        dataset.end();
        System.out.println("Done...Exiting");

    }

    private static void createRDFKnowledgeBase(Dataset dataset, Model model) throws IOException {
        File f = new File("jsons-clean");

        for (File file : f.listFiles()) {
            ObjectMapper objectMapper = new ObjectMapper();
            Person p = objectMapper.readValue(file, Person.class);

            System.out.println(p.getRelationships());



            // make a TDB-backed dataset

            //Creating Base resources
           Resource PERSON = model.createResource(BASE_NAMESPACE+p.getId());

           PERSON.addProperty(FOAF.name, p.getName().replace("_",""));

            PERSON.addProperty(FOAF.depiction,  p.getRelList());




        }

        dataset.commit();
        dataset.end();

        // write outputs
        dataset.begin(ReadWrite.READ);
        try {
           model.write(new FileOutputStream("Lab2_3_shemachandran.xml"), "RDF/XML");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        dataset.end();
    }

    private static void createPersonForName(String name) throws IOException {
        Set<String> nodeIds = populateNodeIdsForName(name);


        for (String nodeId : nodeIds) {
                Person person = createPerson(nodeId, name);
                if (person.isAPerson()) {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.writeValue(new File("jsons/"+person.getId()+".json"), person);
                }
            }


    }

    private static void preprocessRelationship() throws IOException {


        File f = new File("jsons");

        for (File file : f.listFiles()) {
            ObjectMapper objectMapper = new ObjectMapper();
            Person p = objectMapper.readValue(file, Person.class);

            System.out.println(p.getRelationships());

            personMap.put(p.getId(), p);
        }



        for (Person person : personMap.values()) {
            for (String type : person.getTypes()) {
                Set<Person> people = typeWisePeople.get(type);
                if (people == null) {
                    people = new HashSet<>();
                    typeWisePeople.put(type, people);
                }
                people.add(person);
            }
        }


        for (Map.Entry<String, Set<Person>> entry : typeWisePeople.entrySet()) {

            Set<Person> people = entry.getValue();

            int totalCount = people.size();
            Map<String, Integer> relWiseCount = getRelWiseCount(people);

            Set<String> relsToBeRemoved = getRelsToBeRemoved(totalCount, relWiseCount);

            System.out.println("RELS TO BE REMOVED " + relsToBeRemoved + "  " + entry.getKey());

            for (String relToBeRemoved : relsToBeRemoved) {
                for (Person person : people) {
                    person.removeRel(relToBeRemoved);
                }
            }


        }


        for (Person person : personMap.values()) {

            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new File("jsons-clean/"+person.getId()+".json"), person);

        }
    }

    private static Set<String> getRelsToBeRemoved(Integer totalCount, Map<String, Integer> relWiseCount) {
        Set<String> relsToBeRemoved = new HashSet<String>();

        for (Map.Entry<String, Integer> entry : relWiseCount.entrySet()) {
            Double probability = entry.getValue().doubleValue() / totalCount.doubleValue();
            System.out.println("RELATIONSHIP " + entry.getKey() + "  Probability Value " + probability);
            if (probability < 0.5) {
                relsToBeRemoved.add(entry.getKey());
            }
        }

        return relsToBeRemoved;

    }

    private static Map<String, Integer> getRelWiseCount(Set<Person> people) {

        Map<String, Integer> relWiseCount = new HashMap<String, Integer>();

        for (Person person : people) {
            for (String rel : person.getNotHumanRels()) {
                Integer integer = relWiseCount.get(rel);
                relWiseCount.put(rel, integer == null ? new Integer(1) : integer+1);
            }
        }


        return relWiseCount;
    }

    private static Set<String> populateNodeIdsForName(String name) {
        Set<String> nodeIds = new HashSet<>();

        String sparqlQuery = "SELECT DISTINCT ?ref  WHERE {\n" +
                "               ?url <http://www.w3.org/2000/01/rdf-schema#label> \""+name+"\"@en .\n" +
                "                ?url <http://www.lemon-model.net/lemon#sense> ?sense .\n" +
                "                ?sense <http://www.lemon-model.net/lemon#reference> ?ref .\n" +
                "                OPTIONAL {\n" +
                "                ?ref <http://babelnet.org/model/babelnet#definition> ?definition .\n" +
                "                ?definition <http://www.lemon-model.net/lemon#language> \"EN\" .\n" +
                "                ?definition <http://babelnet.org/model/babelnet#gloss> ?gloss .\n" +
                "               \n" +
                "                }\n" +
                "     }";
        String babelNetEndPoint = "http://babelnet.org/sparql/";

        Query query = QueryFactory.create(sparqlQuery);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(
                babelNetEndPoint, query);

        ((QueryEngineHTTP)qexec).addParam("key", "a6324ea1-0326-4e1b-a24a-57f9f12be4a5");

        ResultSet rs = qexec.execSelect();

        while (rs.hasNext()) {
            QuerySolution qs = rs.next();

            String szVal = qs.get("ref").toString();
            String[] split = szVal.split("/");
            String node = split[split.length - 1];
            String nodeId = node.replaceFirst("s","");

            nodeIds.add(nodeId);

        }

        qexec.close();

        return nodeIds;
    }

    private static Person createPerson(String nodeId, String name) {
        try {

            String httpEndPoint = "http://babelnet.org/synset?word=bn:";

            DefaultHttpClient httpClient = new DefaultHttpClient();

            String fullurl = httpEndPoint+nodeId;

            System.out.println(fullurl);

            HttpGet getRequest = new HttpGet(fullurl);
           getRequest.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");

            getRequest.addHeader("User-Agent", "Mozilla/5.0");
            getRequest.addHeader("Accept-Language","en-US,en;q=0.9");
            getRequest.addHeader("Connection","keep-alive");
            getRequest.addHeader("Host","babelnet.org");
            getRequest.addHeader("Upgrade-Insecure-Requests","1");

            HttpResponse response = httpClient.execute(getRequest);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));

            String output;


            Map<String, Set<String>> map = new HashMap<String, Set<String>>();

            while ((output = br.readLine()) != null) {


                if (output.contains("edge-type")&& !output.contains(".edge-type")) {
                    String target = output.replaceAll("<[^>]*>", "").trim();


                    if (target.contains("SEX") || target.contains("GENDER") || target.contains("GIVEN"))
                        continue;

                    Set<String> objects = new HashSet<String>();

                    while((output = br.readLine()) != null && !(output.contains("fix bug versoin"))) {

                        if (output.contains("a  style") && output.contains("lang=EN")) {
                            String target0 = output.replaceAll("<[^>]*>", "").trim();
                            objects.add(target0);
                        }
                    }

                    if (!objects.isEmpty()) {

                        if (target.contains("IS A") || target.contains("OCCUPATION") || target.contains("CITIZEN")
                                || target.contains("TEAM") || target.contains("CLUB")) {
                            objects.forEach(o -> map.put(o, null));
                        }
                        else {

                            map.put(target, objects);
                        }
                    }
               }

            }
            httpClient.getConnectionManager().shutdown();

            System.out.println(map);
            return new Person(name.toUpperCase(), nodeId, map);



        } catch (ClientProtocolException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();
        }

        return null;
    }
}
