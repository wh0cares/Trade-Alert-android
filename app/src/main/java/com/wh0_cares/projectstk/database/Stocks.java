package com.wh0_cares.projectstk.database;

public class Stocks {

    private int id;
    private String symbol;
    private String nextUpdate;

    public Stocks(){
    }

//    public Stocks(int id, String symbol, String nextUpdate){
//        this.id = id;
//        this.symbol = symbol;
//        this.nextUpdate = nextUpdate;
//    }

    public Stocks(String symbol, String nextUpdate){
        this.symbol = symbol;
        this.nextUpdate = nextUpdate;
    }

    public Stocks(String symbol) {
        this.symbol = symbol;
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


}