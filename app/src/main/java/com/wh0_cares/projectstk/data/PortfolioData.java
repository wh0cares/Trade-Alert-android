package com.wh0_cares.projectstk.data;

public class PortfolioData {
    public String firstLetter;
    public String name;
    public String index;
    public String symbol;
    public int id;

    public PortfolioData() {
    }

    public PortfolioData(String firstLetter, String name, String index, String symbol, int id) {
        this.firstLetter = firstLetter;
        this.name = name;
        this.index = index;
        this.symbol = symbol;
        this.id = id;
    }

    public String getFirstLetter() {
        return firstLetter;
    }
    public String getName() {
        return name;
    }
    public String getIndex() {
        return index;
    }
    public String getSymbol() {
        return symbol;
    }

    public int getID() {
        return id;
    }

    public void setFirstLetter(String firstLetter) {
        this.firstLetter = firstLetter;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setIndex(String index) {
        this.index = index;
    }
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setID(int id) {
        this.id = id;
    }

}
