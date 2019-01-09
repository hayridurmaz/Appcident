package tr.edu.tedu.appcident;

/*
* Class Name: User
* Created:10.01.1019
* Author: Hayri Durmaz
*
* It represents User objects stored in Firebase Database
*
* */
public class User {
    public String number1, number2, number3, imei;
    int recordTime;

    public User (String number1, String number2, String number3, String imei, int recordTime ){
        this.number1 = number1;
        this.number2 = number2;
        this.number3 = number3;
        this.imei=imei;
        this.recordTime=recordTime;
    }
}
