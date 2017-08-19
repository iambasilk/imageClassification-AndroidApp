package com.yep.user21.internprj1;

/**
 * Created by user21 on 19-08-2017.
 */

public class MyData {
    //name and address string
    public String desc;
    public String location;
    public String image;

    public MyData() {
      /*Blank default constructor essential for Firebase*/
    }
    public String getDesc(){
        return desc;
    }

    public void setDesc(String des) {
        this.desc = des;
    }


    public void setImage(String img)
    {
        this.image=img;
    }

    public void setLocation(String loc) {
        this.location = loc;
    }
}