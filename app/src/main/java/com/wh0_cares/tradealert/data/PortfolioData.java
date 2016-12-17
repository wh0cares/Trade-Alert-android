package com.wh0_cares.tradealert.data;

public class PortfolioData {
    public String firstLetter;
    public String name;
    public String index;
    public String symbol;

    public PortfolioData() {
    }

    public PortfolioData(String firstLetter, String name, String index, String symbol) {
        this.firstLetter = firstLetter;
        this.name = name;
        this.index = index;
        this.symbol = symbol;
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

}
