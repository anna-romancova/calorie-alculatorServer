package edu.itstap.calculator.Model;


import com.google.gson.Gson;
import edu.itstap.calculator.Food;
import edu.itstap.calculator.FoodInHistory;
import edu.itstap.calculator.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

public class Data {

    private Connection connect = null;
    private Statement statement = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;

    public void readDataBase() throws Exception {
        try {

            Class.forName("com.mysql.jdbc.Driver");

            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost:3306/calculatorCalorie?verifyServerCertificate=false&useSSL=true&"
                            + "user=root&password=root&zeroDateTimeBehavior=convertToNull");


            statement = connect.createStatement();

            resultSet = statement
                    .executeQuery("select * from calculatorCalorie.Users");
            writeResultSet(resultSet);



        } catch (Exception e) {
            throw e;
        }

    }
    private  void  connection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connect = DriverManager
                    .getConnection("jdbc:mysql://localhost:3306/calculatorCalorie?verifyServerCertificate=false&useSSL=true&"
                            + "user=root&password=root");
            statement = connect.createStatement();



        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public boolean authorization( String username, String password){
        boolean aut=false;
        connection();
        try {
            resultSet = statement
                    .executeQuery("select username, password from calculatorCalorie.Users where username= '"+username+"' AND password='"+password+"'");
            if (resultSet.next()) {
                String name =resultSet.getString("username");
                String pas =resultSet.getString("password");
                System.out.println("get data from DB");
                if(name.equals(username) && password.equals(pas)){
                    aut=true;
                }

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return aut;

    }


    public boolean registration(String username, String password, String email){
        connection();
        boolean reg=false;
        try {
            preparedStatement = connect
                    .prepareStatement("insert into  calculatorCalorie.Users (username, email, password, goals_calories ) values (?,?,?,?)");

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, email);
            preparedStatement.setString(3, password);
            preparedStatement.setString(4, "0.0");
            preparedStatement.executeUpdate();
            System.out.println("insert user!");
            reg=true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reg;
    }

    public  ResultSet searchFood(Food food){
        connection();
        try {
            preparedStatement = connect
                    .prepareStatement("SELECT * from calculatorCalorie.foods WHERE name LIKE '%"+food.getName() +"%'");
            resultSet = preparedStatement.executeQuery();


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultSet;


    }

    private void writeResultSet(ResultSet resultSet) throws SQLException {

        while (resultSet.next()) {

            String name = resultSet.getString("name");
            String proteins = resultSet.getString("proteins");
            String fats = resultSet.getString("fats");
            String carbohydrates = resultSet.getString("carbohydrates");
            String 	calories = resultSet.getString("calories");

            System.out.println("name: " + name);
            System.out.println("proteins: " + proteins);
            System.out.println("fats: " + fats);
            System.out.println("carbohydrates: " + carbohydrates);
            System.out.println("calories: " + calories);
        }
    }
    private ArrayList<Food> getRezult(ResultSet resultSet) throws SQLException{
        ArrayList<Food> resultSearch=new ArrayList<>();

        while (resultSet.next()) {

            String name = resultSet.getString("name");
            String proteins = resultSet.getString("proteins");
            String fats = resultSet.getString("fats");
            String carbohydrates = resultSet.getString("carbohydrates");
            String 	calories = resultSet.getString("calories");
            Food food=new Food(name,Double.valueOf(calories),Double.valueOf(proteins),Double.valueOf(fats),Double.valueOf(carbohydrates));
            resultSearch.add(food);
        }
        return resultSearch;
    }


    public  boolean addNewProduct(String name,Double proteins,Double fats,Double carbohydrates,Double calories){
        connection();
        boolean add=false;
        try {
            preparedStatement = connect
                    .prepareStatement("insert into  calculatorCalorie.foods (name, proteins, fats, carbohydrates, calories ) values (?,?,?,?,?)");
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, String.valueOf(proteins));
            preparedStatement.setString(3, String.valueOf(fats));
            preparedStatement.setString(4, String.valueOf(carbohydrates));
            preparedStatement.setString(5, String.valueOf(calories));
            preparedStatement.executeUpdate();
            System.out.println("insert food!");
            add=true;
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return add;
    }


    public void close() {
        try {
            if (resultSet != null) {
                resultSet.close();
            }

            if (statement != null) {
                statement.close();
            }

            if (connect != null) {
                connect.close();
            }
        } catch (Exception e) {

        }
    }

    public ResultSet getEmail(String userName, String password) {

        connection();
        try {

            resultSet = statement
                    .executeQuery("select  id , email , goals_calories , useSqLite  from calculatorCalorie.Users where username= '"+userName+"' AND password='"+password+"'");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultSet;

    }
    public void saveGoal(User user){
        connection();
        String goal=String.valueOf(user.getGoalOfCalories());
        try {
            preparedStatement = connect
                    .prepareStatement(" UPDATE calculatorCalorie.Users  SET `goals_calories`='"+goal+"' WHERE id="+user.getId());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("insert goal!");

    }

    public void updateUser(User user) {
        connection();
        try {
            preparedStatement = connect
                    .prepareStatement(" UPDATE calculatorCalorie.Users  SET username = ?, email = ?, password = ?, goals_calories = ?,useSqLite=? WHERE id = ?");
            preparedStatement.setString(1, user.getUserName());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, user.getPassword());
            preparedStatement.setString(4, String.valueOf(user.getGoalOfCalories()));
            preparedStatement.setInt(5, user.getId());
            preparedStatement.setBoolean(6, user.isUseSqLite());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean saveHistoryMenu(User user) {
        connection();
        boolean save=false;
        Gson gs =new Gson();
        String history= gs.toJson(user.getHistoryFoods().get(0));

       if(!user.getHistoryFoods().get(0).isEmpty()) {
           try {
               preparedStatement = connect
                       .prepareStatement("insert into  calculatorCalorie.historyMenu (user, Date, menu ) values (?,?,?)");
               preparedStatement.setInt(1, user.getId());
               preparedStatement.setTimestamp(2, user.getHistoryFoods().get(0).get(0).getTime());
               preparedStatement.setString(3,history);
               preparedStatement.executeUpdate();
               System.out.println("insert history!");
               save = true;
           } catch (SQLException e) {
               e.printStackTrace();
           }

       }

        return save;
    }

    public ResultSet getAllHistoryOfMenu( User user) {
        connection();
        try {
            resultSet = statement
                    .executeQuery("select * from calculatorCalorie.historyMenu where user="+user.getId());

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultSet;

    }

    public boolean cleanHistory(User user) {
        connection();
        boolean del=false;
        try {
            preparedStatement = connect
                    .prepareStatement("delete from calculatorCalorie.historyMenu where user= ? ; ");
            preparedStatement.setInt(1, user.getId());
            preparedStatement.executeUpdate();
            del=true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return del;

    }
}
