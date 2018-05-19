package com.sw.osws.view;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;

import java.util.ArrayList;
import java.util.List;

public class OSWSAutoComplete implements AutoCompletable<RenderItem, String> {
    public List<RenderItem> autoComplete(String prefix) {

        List<RenderItem> renderItems = new ArrayList<>();
        renderItems.add(new RenderItem(prefix, true));
        String directory = "KB" ;
        Dataset dataset = TDBFactory.createDataset(directory);
        Model model = dataset.getNamedModel("myrdf");

        dataset.begin(ReadWrite.READ);

        String q = "SELECT ?n ?result\n" +
                "WHERE { ?s <http://xmlns.com/foaf/0.1/name> ?n \n" +
                "FILTER regex(?n,'^"+prefix+"','i') . \n" +
                "?s <http://xmlns.com/foaf/0.1/depiction> ?result}";

        Query query = QueryFactory.create(q);
        QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
        ResultSet resultSet = queryExecution.execSelect();

        while (resultSet.hasNext()) {
            QuerySolution querySolution = resultSet.nextSolution();
            String name = ((Literal) querySolution.get("n")).getString();
            Literal suggestionsLit = (Literal) querySolution.get("result");

            String[] suggestions = suggestionsLit.getString().split(",");

            if (suggestions.length == 0)
                continue;

            for (int i = 0; i < suggestions.length - 1; i++) {
                renderItems.add(new RenderItem(name+ " " + suggestions[i], false));
            }

            renderItems.add(new RenderItem(name + " " + suggestions[suggestions.length - 1], true));

        }


        queryExecution.close();


        dataset.end();

        return renderItems;
    }


    public static void main(String[] args) {
        OSWSAutoComplete a = new OSWSAutoComplete();
        System.out.println(a.autoComplete("mich"));
    }
}
