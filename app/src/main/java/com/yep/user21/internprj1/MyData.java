package com.yep.user21.internprj1;

/**
 * Created by user21 on 19-08-2017.
 */

public class MyData {
    //name and address string
    public String desc;
    public String location;

    public MyData() {
      /*Blank default constructor essential for Firebase*/
    }

    public void setDesc(String des) {
        this.desc = des;
    }



    public void setLocation(String loc) {
        this.location = loc;
    }
}