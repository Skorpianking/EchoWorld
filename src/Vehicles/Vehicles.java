package Vehicles;

import com.github.cliftonlabs.json_simple.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Vehicles implements Jsonable {

        private String name;
        private int age;
        private String[] position;
        private List<String> skills;

        public Vehicles(String name, int age, String[] position, List<String> skills) {
            this.name = name;
            this.age = age;
            this.position = position;
            this.skills = skills;
        }

        //..getters setters

        @Override
        public String toJson() {
            final StringWriter writable = new StringWriter();
            try {
                this.toJson(writable);
            } catch (final IOException e) {
            }
            return writable.toString();
        }

        @Override
        public void toJson(Writer writer) throws IOException {

            final JsonObject json = new JsonObject();
            json.put("name", this.getName());
            json.put("age", this.getAge());
            json.put("position", this.getPosition());
            json.put("skills", this.getSkills());
            json.toJson(writer);
        }

        public static void main(String[] args) {
            try {
                Vehicles simulation = new Vehicles("test", 12, new String[]{"Founder", "CTO", "Writer"}, Arrays.asList("java", "python", "node", "kotlin"));
                simulation.run();
            } catch(Exception e) {
                System.out.println("FAIL:" + e);
            }
        }
        public void run() {
            String test = this.toJson();
            test = Jsoner.prettyPrint(test);
            System.out.println(test);

            // Java objects to JSON file
            try(FileWriter fileWriter = new FileWriter("test.json")) {
                Jsoner.serialize(test, fileWriter);
            } catch(Exception e) {
                System.out.println("FAIL:" + e);
            }
            JsonObject deserialize = null;

            try (FileReader fileReader = new FileReader(("vehicle.json"))) {
                deserialize = (JsonObject) Jsoner.deserialize(fileReader);
            } catch(Exception e) {
                System.out.println("FAIL:" + e);
            }

            System.out.println(deserialize);
            String vehicleName = (String)deserialize.get("vehicleName");
            treeFromJSON(deserialize);

        }

        public void treeFromJSON(JsonObject json) {
            JsonArray array;
            array = (JsonArray) json.get("behaviorTree");
            for (Object item : array) {
                JsonObject behavior = (JsonObject) item;
                boolean arbiter = (boolean) behavior.get("arbiter");
                String name = (String) behavior.get("name");
                System.out.println("name: " + name);
                if (arbiter) {
                    System.out.println("Composite");
                    treeFromJSON(behavior);
                    ArrayList<Double> weights = (ArrayList)behavior.get("weights");
                    System.out.println("Weights: " + weights);
                    System.out.println("end");
                }
            }
        }


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public String[] getPosition() {
            return position;
        }

        public void setPosition(String[] position) {
            this.position = position;
        }

        public List<String> getSkills() {
            return skills;
        }

        public void setSkills(List<String> skills) {
            this.skills = skills;
        }
    }
