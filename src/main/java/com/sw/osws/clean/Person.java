package com.sw.osws.clean;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.*;

public class Person implements Serializable {

    private String name;
    private String id;
    private Map<String, Set<String>> relationships;

    public Person() {}

    public Person(String name, String id, Map<String, Set<String>> relationships) {
        this.name = name;
        this.id = id;
        this.relationships = relationships;
    }


    @JsonIgnore
    public boolean isAPerson() {
        return relationships.containsKey("human");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Set<String>> getRelationships() {
        return relationships;
    }

    public void setRelationships(Map<String, Set<String>> relationships) {
        this.relationships = relationships;
    }

    public Set<String> getTypes() {

        Set<String> types = new HashSet<String>();

        for (String key : relationships.keySet()) {
            if (relationships.get(key) == null)
                if (!key.equals("human"))
                    types.add(key);
        }

        return types;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Person person = (Person) o;

        return id.equals(person.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public void removeRel(String relsToBeRemoved) {
        relationships.remove(relsToBeRemoved);
    }

    public Set<String> getNotHumanRels() {
        Set<String> notHumanRels = new HashSet<String>();

        for (String key : relationships.keySet()) {
            if (relationships.get(key) == null)
                continue;
             notHumanRels.add(key);
        }

        return notHumanRels;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", relationships=" + relationships +
                '}';
    }

    @JsonIgnore
    public String getRelList() {

        List<String> rels = new ArrayList<>(relationships.keySet());



        rels.remove("human");

        if (rels.isEmpty())
        return "";

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < rels.size() - 1; i++) {
            sb.append(rels.get(i));
            sb.append(",");
        }

        sb.append(rels.get(rels.size()-1));

        return sb.toString();
    }
}
