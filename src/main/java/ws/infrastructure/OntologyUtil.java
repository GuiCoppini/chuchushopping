package ws.infrastructure;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import ws.WsApplication;

public class OntologyUtil {

    private static final String TURTLE_LANGUAGE = "TTL";

    protected static final String schema = "http://www.semanticweb.org/hozer/ontologies/2019/9/untitled-ontology-2#";
    private static final File owl = new File(Objects.requireNonNull(WsApplication.class.getClassLoader().getResource("ws.ttl")).getFile());
    private static final Model model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
    private static final Model modelWithoutInf = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
    private static final String sparQLPrefixes =
            "PREFIX : <http://www.semanticweb.org/hozer/ontologies/2019/9/untitled-ontology-2#> \n" + "PREFIX owl: <http://www.w3" +
                    ".org/2002/07/owl#> \n" + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + "PREFIX xml: <http://www" +
                    ".w3.org/XML/1998/namespace> \n" + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" + "PREFIX rdfs: <http://www" +
                    ".w3.org/2000/01/rdf-schema#> \n\n";
    private static final Reasoner r = ReasonerRegistry.getOWLReasoner();
    private static final InfModel inf = ModelFactory.createInfModel(r, model);

    static {
        model.read(owl.getAbsolutePath(), TURTLE_LANGUAGE);
        model.add(inf);
    }

    public static String sparql(String query) {
        String fullQuery = sparQLPrefixes + query;
        Query q = QueryFactory.create(fullQuery);
        QueryExecution qe = QueryExecutionFactory.create(q, model);

        ResultSet res = qe.execSelect();

        return toJson(res);
    }

    public static String executeSparqlWithSpecificModel(String query, Model model) {
        String fullQuery = sparQLPrefixes + query;
        Query q = QueryFactory.create(fullQuery);
        QueryExecution qe = QueryExecutionFactory.create(q, model);

        ResultSet res = qe.execSelect();

        return toJson(res);
    }

    public static List<Individual> getAllIndividualsOfType(String type) {
        Resource clazz = model.getResource(schema + type);
        return ((OntModel) model).listIndividuals(clazz).toList();
    }

    public static List<Individual> getAllIndividualsOfType(String type, Model specificModel) {
        Resource clazz = specificModel.getResource(schema + type);
        return ((OntModel) specificModel).listIndividuals(clazz).toList();
    }

    public static Model model() {
        return model;
    }

    public static Property getProperty(String property) {
        return model.getProperty(schema + property);
    }

    public static Property getProperty(String schema, String property) {
        return model.getProperty(schema + property);
    }

    public static String getSpaceType(Resource r) {
        Resource resWithoutInf = modelWithoutInf.getResource(r.getURI());
        Property typeProperty = modelWithoutInf.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

        resWithoutInf.listProperties(typeProperty);
        List<String> types = r.listProperties(typeProperty).toList().stream()
                .map(stmt -> stmt.getResource().getLocalName())
                .collect(Collectors.toList());

        if (types.contains("CommercialCenter")) {
            return "CommercialCenter";
        }
        if (types.contains("Gateway")) {
            return "Gateway";
        }
        if (types.contains("Walkable")) {
            return "Walkable";
        }
        if (types.contains("Obstacle")) {
            return "Obstacle";
        }

        return "Unknown";
    }

    public static List<String> getCategories() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode jsonNode = objectMapper.readTree(sparql("SELECT ?category WHERE {" +
                " [a :Category;" +
                "rdfs:label ?category" +
                "]}"));

        List<String> categories = new ArrayList<>();

        jsonNode.get("results").get("bindings").findValues("category").forEach(cat -> categories.add(cat.get("value").asText()));

        return categories;
    }

    public static String defaultSchema() {
        return schema;
    }

    private static String toJson(ResultSet resultSet) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ResultSetFormatter.outputAsJSON(outputStream, resultSet);

        return new String(outputStream.toByteArray());
    }

    // Straight to the trash
    public static Model sparqlModel(String query) {
        ResultSet res = getResultSetFromQuery(query);

        return res.getResourceModel();
    }

    //
    private static ResultSet getResultSetFromQuery(String query) {
        String fullQuery = sparQLPrefixes + query;
        Query q = QueryFactory.create(fullQuery);
        QueryExecution qe = QueryExecutionFactory.create(q, model);

        return qe.execSelect();
    }

    public static List<Property> getSpaceProperties(Model model, String schema) {
        Property topOf = model.getProperty(schema + "topOf");
        Property bottomOf = model.getProperty(schema + "bottomOf");
        Property leftOf = model.getProperty(schema + "leftOf");
        Property rightOf = model.getProperty(schema + "rightOf");
        Property connects = model.getProperty(schema + "connects");
        Property belongsTo = model.getProperty(schema + "belongsTo");
        Property floor = model.getProperty(schema + "floor");

        return Arrays.asList(topOf, bottomOf, leftOf, rightOf, connects, belongsTo, floor);
    }

    public static Individual getCommercialCenterIndividual() {
        return OntologyUtil.getAllIndividualsOfType("CommercialCenter", model).get(0);
    }

    static List<Property> getUserProperties() {
        Property cpf = model.getProperty(schema + "cpf");
        Property celphone = model.getProperty(schema + "celphone");
        Property likes = model.getProperty(schema + "likes");

        return Arrays.asList(cpf, celphone, likes);
    }

    static Resource getResourceWithName(String name) {
        return model.getResource(schema + name);
    }

    static void writeData(OntModel ontModel, String path) {
        try (OutputStream out = new FileOutputStream(path)) {
            ontModel.write(out, "TURTLE");
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
