package com.mscharhag.jsonschema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.*;

import java.io.InputStream;
import java.util.Set;

public class Main {

    private static InputStream inputStreamFromClasspath(String path) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    }

    public static void main(String[] args) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);

        try (
                InputStream jsonStream = inputStreamFromClasspath("example.json");
                InputStream schemaStream = inputStreamFromClasspath("example-schema.json")
        ) {
            JsonNode json = objectMapper.readTree(jsonStream);
            JsonSchema schema = schemaFactory.getSchema(schemaStream);
            Set<ValidationMessage> validationResult = schema.validate(json);

            // print validation errors
            if (validationResult.isEmpty()) {
                System.out.println("no validation errors :-)");
            } else {
                validationResult.forEach(vm -> {
                    System.out.println(vm.getMessage());
                    String[] path = vm.getPath().split("\\.");
                    int pathLength = path.length;
                    if(vm.getType().equals("maxLength")){
                        System.out.println("maxLength : " + vm.getPath());
                        if(pathLength == 2){
                            ((ObjectNode)json).put(path[1], json.get(path[1]).toString().substring(0, 50));
                        }
                    } else if (vm.getType().equals("required")){
                        System.out.println("required : " + vm.getPath());
                    } else if (vm.getType().equals("multipleOf")){
                        System.out.println("multipleOf : " + vm.getPath());
                        if(pathLength == 2){
                            JsonNode numberNode = json.get(vm.getPath().substring(2));
                            Double number = numberNode!= null ? numberNode.asDouble() : null;
                            ((ObjectNode)json).put(path[1], (double)Math.round(number * 100d) / 100d);
                        }
                    } else if (vm.getType().equals("enum")){
                        System.out.println("enum : " + vm.getPath());
                    }
                });
                System.out.println(json);
            }
        }
    }
}
