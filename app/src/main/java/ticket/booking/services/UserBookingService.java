package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.Ticket;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.sql.Time;
import java.util.*;
import java.util.stream.Collectors;

public class UserBookingService
{
    private User user;

    private List<User> userList;
    private ObjectMapper objectMapper = new ObjectMapper();
    private static final String USERS_PATH = "app/src/main/java/ticket/booking/localDb/users.json";

    public UserBookingService(User user1) throws IOException {
        this.user = user1;
        userList = loadUsers();
    }

    public UserBookingService()throws  IOException{
        userList = loadUsers();
    }
    public List<User> loadUsers() throws IOException{
        File users = new  File(USERS_PATH);
        return objectMapper.readValue(users, new TypeReference<List<User>>() {});
    }



    public Boolean loginUser(){
        System.out.println("Helllo there");
        Optional<User> foundUser = userList.stream().filter(userObj -> {
            return userObj.getName().equalsIgnoreCase(user.getName()) && UserServiceUtil.checkPassword(user.getPassword(), userObj.getHashPassword());
        }).findFirst();
        foundUser.ifPresent(value -> user = value);
        return foundUser.isPresent();
    }

    public  Boolean signUp(User user1){
        try{
            userList.add(user1);
            saveUserListToFile();
            return Boolean.TRUE;
        }catch (IOException ex){
            return Boolean.FALSE;
        }
    }

    private void saveUserListToFile() throws  IOException {
        File userFile = new File(USERS_PATH);
        objectMapper.writeValue(userFile, userList);
    }

    public void fetchBooking(){
        user.printTickets();
    }

    public Boolean cancelBooking(String ticketId){

        if (user.getTicketsBooked() == null || user.getTicketsBooked().isEmpty()) {
            return Boolean.FALSE;
        }

        List<Ticket> updatedTicketsBooked = user.getTicketsBooked().stream()
                .filter(ticket -> !ticket.getTicketId().equals(ticketId))
                .collect(Collectors.toList());

        if(updatedTicketsBooked.size() == user.getTicketsBooked().size()){
            return Boolean.FALSE;
        }
        user.setTicketsBooked(updatedTicketsBooked);
        userList = userList.stream()
                .map(userObj-> userObj.getName().equals(user.getName()) ? user: userObj)
                .collect(Collectors.toList());

        try{
            saveUserListToFile();
            return Boolean.TRUE;
        } catch (IOException e) {
            return Boolean.FALSE;
        }

    }

    public List<Train> getTrains(String source, String destination){
        try{
            TrainService trainService = new TrainService();
            System.out.println("Inside getTrains");
            return trainService.searchTrains(source, destination);

        }catch(Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        }

    }

    public List<List<Integer>> fetchSeats(Train train){

        return train.getSeats();
    }


    public boolean bookTrainSeat(Train train, int row, int col){
        try{
            TrainService trainService = new TrainService();
            List<List<Integer>> seats = train.getSeats();

            if(row>= 0 && row < seats.size() && col >= 0 && col < seats.get(row).size()){
                if(seats.get(row).get(col) == 0){
                    seats.get(row).set(col, 1);
                    train.setSeats(seats);
                    trainService.updateTrain(train);
                    return true;
                }
                else{
                    return false;
                }
            }else{
                return false;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public void saveTicket(Train train, String source, String destination ){
        Ticket ticket = new Ticket(UUID.randomUUID().toString(), user.getUserId(), source, destination, "dd:mm:yy",train);
        List<Ticket> userTickets= user.getTicketsBooked();
        userTickets.add(ticket);
        user.setTicketsBooked(userTickets);

        userList = userList.stream().map(userObj -> userObj.getUserId().equals(user.getUserId())? user: userObj).collect(Collectors.toList());
        try{
            saveUserListToFile();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
