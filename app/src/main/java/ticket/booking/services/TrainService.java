package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.Train;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TrainService {
    private Train train;
    private List<Train> trainList;
    private ObjectMapper objectMapper = new ObjectMapper();
    private static final String TRAIN_PATH = "app/src/main/java/ticket/booking/localDb/trains.json";

    public TrainService() throws  IOException{
        loadTrains();
    }
    public  TrainService(Train train) throws  IOException{
        this.train = train;
        loadTrains();
    }
    public void loadTrains() throws IOException{
        File trains = new File(TRAIN_PATH);
        trainList = objectMapper.readValue(trains, new TypeReference<List<Train>>(){});
    }

    public List<Train> searchTrains(String source, String destination){
        System.out.println("Inside search train service");
        System.out.println(trainList);
        return trainList.stream().filter( train -> validTrain(train, source,  destination)).collect(Collectors.toList());

    }
    public boolean validTrain(Train train, String source, String destination){
        List<String> stations = train.getStations();
        int srcIndex = stations.indexOf(source);
        int desIndex = stations.indexOf(destination);
        return srcIndex != -1 && desIndex != -1 && srcIndex < desIndex;
    }


    public void updateTrain(Train updatedTrain){
        OptionalInt id = IntStream.range(0, trainList.size())
                .filter( i -> trainList.get(i).getTrainId().equalsIgnoreCase(updatedTrain.getTrainId()))
                .findFirst();
        if(id.isPresent()){
            trainList.set(id.getAsInt(), updatedTrain);
            saveTrainListToFile();
        }

    }
    private void saveTrainListToFile() {
        try {
            objectMapper.writeValue(new File(TRAIN_PATH), trainList);
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception based on your application's requirements
        }
    }
}
