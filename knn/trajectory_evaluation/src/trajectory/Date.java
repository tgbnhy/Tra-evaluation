/*
 * Decompiled with CFR 0_114.
 */
package trajectory;

public class Date {
    int year;
    int month;
    int day;
    int hour;
    int min;
    int sec;

    public Date(int year, int month, int day, int hour, int min, int sec) {
        this.year = year;
        this.day = day;
        this.month = month;
        this.hour = hour;
        this.min = min;
        this.sec = sec;
    }

    public Date(String date, String time) {
        String[] d = date.trim().split("/");
        this.day = new Integer(d[0]);
        this.month = new Integer(d[1]);
        this.year = new Integer(d[2]);
        String[] t = time.trim().split(":");
        this.hour = new Integer(t[0]);
        this.min = new Integer(t[1]);
        this.sec = new Integer(t[2]);
        //12/12/2016 12:12:12
    }

    public String toString() {
        String str = String.valueOf(this.day) + "/" + this.month + "/" + this.year + " " + this.hour + ":" + this.min + ":" + this.sec;
        return str;
    }
}

