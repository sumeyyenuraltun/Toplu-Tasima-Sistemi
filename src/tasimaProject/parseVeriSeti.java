package tasimaProject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class parseVeriSeti {
    public List<Duraklar> parseJson() throws IOException {
        File file = new File("src/veriseti.json");

        if (!file.exists()) {
            System.err.println("Error: JSON file not found at: " + file.getAbsolutePath());
            throw new IOException("File not found");
        }

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(file);

        if (rootNode.get("duraklar") == null || !rootNode.get("duraklar").isArray()) {
            System.err.println("Error: Invalid JSON structure - 'duraklar' array not found");
            throw new IOException("Invalid JSON structure");
        }

        String city = rootNode.has("city") ? rootNode.get("city").asText() : "";

        JsonNode taxiNode = rootNode.get("taxi");
        double openingFee = taxiNode.has("openingFee") ? taxiNode.get("openingFee").asDouble() : 0.0;
        double costPerKm = taxiNode.has("costPerKm") ? taxiNode.get("costPerKm").asDouble() : 0.0;

        List<Duraklar> durakList = new ArrayList<>();
        JsonNode duraklarArray = rootNode.get("duraklar");

        for (JsonNode durakNode : duraklarArray) {
            String id = durakNode.has("id") ? durakNode.get("id").asText() : "";
            String name = durakNode.has("name") ? durakNode.get("name").asText() : "";
            String type = durakNode.has("type") ? durakNode.get("type").asText() : "";
            double lat = durakNode.has("lat") ? durakNode.get("lat").asDouble() : 0.0;
            double lon = durakNode.has("lon") ? durakNode.get("lon").asDouble() : 0.0;
            boolean sonDurak = durakNode.has("sonDurak") ? durakNode.get("sonDurak").asBoolean() : false;

            ArrayList<nextStops> nextstoplist = new ArrayList<>();
            ArrayList<Transfer> transferlist = new ArrayList<>();

            if (durakNode.has("transfer") && !durakNode.get("transfer").isNull()) {
                JsonNode transferNode = durakNode.get("transfer");
                String transferStopId = transferNode.has("transferStopId") ?
                        transferNode.get("transferStopId").asText() : "";
                double transferSure = transferNode.has("transferSure") ? transferNode.get("transferSure").asDouble() : 0;
                double transferUcret = transferNode.has("transferUcret") ? transferNode.get("transferUcret").asDouble() : 0.0;

                Transfer transfer = new Transfer(transferStopId, transferSure, transferUcret);
                transferlist.add(transfer);
            }

            if (durakNode.has("nextStops") && durakNode.get("nextStops").isArray()) {
                JsonNode nextStopsArray = durakNode.get("nextStops");
                if (nextStopsArray.size() > 0) {  // Check if nextStops array is not empty
                    for(int i = 0; i < nextStopsArray.size(); i++) {
                        JsonNode firstNextStopNode = nextStopsArray.get(i); // Get the first nextStop
                        String stopId = firstNextStopNode.has("stopId") ? firstNextStopNode.get("stopId").asText() : "";
                        double mesafe = firstNextStopNode.has("mesafe") ? firstNextStopNode.get("mesafe").asDouble() : 0.0;
                        double sure = firstNextStopNode.has("sure") ? firstNextStopNode.get("sure").asDouble() : 0.0;
                        double ucret = firstNextStopNode.has("ucret") ? firstNextStopNode.get("ucret").asDouble() : 0.0;

                        nextStops nextStopObj = new nextStops(stopId, mesafe, sure, ucret);
                        nextstoplist.add(nextStopObj);
                    }
                } else {
                    System.out.println("Warning: 'nextStops' array is empty for Durak ID: " + id);
                }
            }
            Vehicle vehicle = VehicleFactory.createVehicle(type, nextstoplist, transferlist);

            Duraklar durak = new Duraklar(id, name, type, lat, lon, sonDurak, vehicle);

            durakList.add(durak);
        }

        return durakList;
    }
}
