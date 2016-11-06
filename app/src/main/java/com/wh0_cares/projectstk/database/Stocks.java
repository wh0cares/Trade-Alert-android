package com.wh0_cares.projectstk.database;

public class Stocks {

    private int id;
    private String symbol;

    public Stocks(){
    }

    public Stocks(int id, String symbol){
        this.id = id;
        this.symbol = symbol;
    }

    public Stocks(String symbol){
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
}