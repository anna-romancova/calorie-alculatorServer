package edu.itstap.calculator.Model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import edu.itstap.calculator.Food;
import edu.itstap.calculator.FoodInHistory;
import edu.itstap.calculator.User;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.lang.reflect.Type;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


public class UserServer implements Runnable {

    private boolean authorization;
    private boolean reg;
    private Socket socket;
    private ObjectInputStream ois;
    private User user;
    private ObjectOutputStream oos;
    private Data dt;



    public UserServer(Socket socket) {
        this.socket = socket;
        dt = new Data();

        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();

            ois = new ObjectInputStream(socket.getInputStream());
            user = (User) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


        System.out.println("server constructor");
    }



    public boolean isReg() {
        return reg;
    }



    public boolean authorization() {


        this.authorization = dt.authorization(user.getUserName(), user.getPassword());
        ResultSet dataUser = dt.getEmail(user.getUserName(), user.getPassword());
        user.setAutorization(this.authorization);
        double goal;
        user.setReg(true);
        try {
            if (dataUser.next()) {
              if(dataUser.getString("goals_calories").isEmpty()){
                  goal=0.0;
                }else {
                  goal=Double.valueOf(dataUser.getString("goals_calories"));
               }
                user.setEmail(dataUser.getString("email"));
                user.setId(dataUser.getInt("id"));
                user.setGoalOfCalories(goal);
                user.setUseSqLite(dataUser.getBoolean("useSqLite"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        /*try {
            User us=  allHistoryMenuOfUser(user);
            setUser(us);

        } catch (SQLException e) {
            e.printStackTrace();
        }*/


        try {
            oos.writeObject(user);
            oos.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(user.toString());

        return this.authorization;

    }

    public boolean registration() {
        reg = this.isReg();
        reg = dt.registration(user.getUserName(), user.getPassword(), user.getEmail());
        user.setAutorization(true);
        user.setReg(reg);

        try {
            oos.writeObject(user);
            oos.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return reg;
    }

    public void addNewFood(Food foodAd) {
        boolean add = false;
        add = dt.addNewProduct(foodAd.getName(), foodAd.getProtein(), foodAd.getFats(), foodAd.getCarbohydrate(), foodAd.getCalories());
        if (add) {
            user.getAddFood().clear();
            System.out.println("add food");
            try {
                oos.writeObject(user);
                oos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void searchFood(Food foodSearch) throws SQLException {
        ArrayList<Food> searchFood = new ArrayList<>();
        ResultSet res = dt.searchFood(foodSearch);
        if (!user.getSearchFood().isEmpty()) {
            user.getSearchFood().clear();
        }
        while (res.next()) {
            searchFood.add(new Food(res.getString("name"), Double.valueOf(res.getString("calories")), Double.valueOf(res.getString("proteins")), Double.valueOf(res.getString("fats")), Double.valueOf(res.getString("carbohydrates"))));
        }
        user.setSearchFood(searchFood);
        System.out.println(user.toString());
        try {
            oos.writeObject(user);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public void setUser(User user) {
        this.user = user;
    }


    @Override
    public void run() {
        if(user.getUserName().isEmpty()&&(!user.getSearchFood().isEmpty() && user.getSearchFood().get(0).isSearch())){
            try {
                this.searchFood(user.getSearchFood().get(0));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (!user.getUserName().isEmpty()&& !user.isReg() && !user.isAutorization()) {
            this.registration();
        } else if (user.isReg() && !user.isAutorization()) {
            this.authorization();

        }
        if (user.isAutorization() && !user.getAddFood().isEmpty() && user.getAddFood().get(0).isAdd()) {
            this.addNewFood(user.getAddFood().get(0));
        }

        if(user.isAutorization()&& user.isInsertGoal()){
            this.GoalOfCalories(user);
        }
        if(user.isAutorization()&& user.isProfileUpdate()){
            this.updateUser(user);
        }
        if(user.isAutorization()&& user.isSaveHistoryMenu()){
            this.SaveHistoryMenu(user);
        }
        if (user.isAutorization()&&!user.getSearchFood().isEmpty() && user.getSearchFood().get(0).isSearch()) {
            try {
                this.searchFood(user.getSearchFood().get(0));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (user.isCleanAllHistory()){
            CleanAllHistory(user);
        }
        try {
            oos.close();
            ois.close();
            dt.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void CleanAllHistory(User user) {
      boolean res=   dt.cleanHistory(user);
      if(res){
          user.getHistoryFoods().clear();
          user.setCleanAllHistory(false);
          try {
              oos.writeObject(user);
              oos.flush();
          } catch (IOException e) {
              e.printStackTrace();
          }
      }
    }

    private void SaveHistoryMenu(User user) {
        boolean save;
        save= dt.saveHistoryMenu(user);
        if(save){
            user.getHistoryFoods().clear();
            user.setSaveHistoryMenu(false);


            try {
              User us=  allHistoryMenuOfUser(user);
              setUser(us);

            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                oos.writeObject(user);
                oos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
    private User  allHistoryMenuOfUser(User user) throws SQLException {
        ResultSet res= dt.getAllHistoryOfMenu(user);

        ArrayList<ArrayList<FoodInHistory>> allHistory=new ArrayList<>();
        Gson gs= new Gson();

        while (res.next()){
            String menu = res.getString("menu");
            Type type = new TypeToken<ArrayList<FoodInHistory>>(){}.getType();
            ArrayList<FoodInHistory> fHistory = gs.fromJson(menu, type);
            allHistory.add(fHistory);
            user.setHistoryFoods(allHistory);
        }
        return user;
    }

    private void updateUser(User user) {
        dt.updateUser(user);
        user.setInsertGoal(false);
        try {
            oos.writeObject(user);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    private void GoalOfCalories(User user) {
        dt.saveGoal(user);
        user.setInsertGoal(false);
        try {
            oos.writeObject(user);
            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
