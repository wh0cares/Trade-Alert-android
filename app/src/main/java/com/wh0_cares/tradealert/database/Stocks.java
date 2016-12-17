package com.wh0_cares.tradealert.database;

public class Stocks {

    private int id;
    private String symbol;
    private String name;
    private String index;
    private String nextUpdate;
    private int volAvg;

    public Stocks(String symbol, String name, String index){
        this.symbol = symbol;
        this.name = name;
        this.index = index;
    }

    public Stocks(String symbol, String nextUpdate, int volAvg){
        this.symbol = symbol;
        this.nextUpdate = nextUpdate;
        this.volAvg = volAvg;
    }

    public Stocks(String symbol) {
        this.symbol = symbol;
    }

    public Stocks() {
    }

    public int getID(){
        return this.id;
    }

    public void setID(int id){
        this.id = id;
    }

    public String getSymbol(){
        return this.symbol;
    }

    public void setSymbol(String symbol){
        this.symbol = symbol;
    }

    public String getNextUpdate(){
        return this.nextUpdate;
    }

    public void setNextUpdate(String nextUpdate){
        this.nextUpdate = nextUpdate;
    }

    public int getVolAvg(){
        return this.volAvg;
    }

    public void setVolAvg(int volAvg){
        this.volAvg = volAvg;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getIndex(){
        return this.index;
    }

    public void setIndex(String index){
        this.index = index;
    }


}