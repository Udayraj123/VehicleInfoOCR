package com.example.ocr.Jsoup;

import org.apache.commons.lang3.text.WordUtils;

public class Vehicle
{
    private String number;
    private String name;
    private String fuel;
    private String cc;
    private String engine;
    private String chassis;
    private String owner;
    private String location;
    private String expiry;
    private final String NOT_AVAILABLE = "n/a";

    public Vehicle() {}

    public Vehicle(String number, String name, String fuel, String cc, String engine, String chassis, String owner, String location, String expiry)
    {
        this.number = number;
        this.name = name;
        this.fuel = fuel;
        this.cc = cc;
        this.engine = engine;
        this.chassis = chassis;
        this.owner = owner;
        this.location = location;
        this.expiry = expiry;
    }

    public Vehicle(String number, String name, String fuel, String cc, String engine, String chassis, String owner, String location, String expiry, Boolean dynamic)
    {
        this.number = number;
        setName(cleanString(name));
        setFuel(cleanString(fuel));
        setCc(cleanString(cc));
        setEngine(engine);
        setChassis(chassis);
        setOwner(cleanString(owner));
        setLocation(cleanString(location));
        setExpiry(expiry);
    }

    public void setName(String name)
    {
        if(name.length() == 0)
            this.name = "Vehicle Name "+NOT_AVAILABLE;
        else
            this.name = name;
    }

    public void setFuel(String fuel)
    {
        if(fuel.length() == 0)
            this.fuel = NOT_AVAILABLE;
        else
            this.fuel = fuel;
    }

    public void setCc(String cc)
    {
        if(!cc.toLowerCase().contains("cc") || cc.length() == 0)
            this.cc = NOT_AVAILABLE;
        else
            this.cc = ((cc.toLowerCase().contains("above")) ? "< " : "> ") +  cc.replaceAll("[^0-9]", "") + " cc";
    }

    public void setEngine(String engine)
    {
        if(engine.length() == 0)
            this.engine = NOT_AVAILABLE;
        else
            this.engine = engine;
    }

    public void setChassis(String chassis)
    {
        if(chassis.length() == 0)
            this.chassis = NOT_AVAILABLE;
        else
            this.chassis = chassis;
    }

    public void setOwner(String owner)
    {
        if(owner.length() == 0)
            this.owner = NOT_AVAILABLE;
        else
            this.owner = owner;
    }

    public void setLocation(String location)
    {
        if(location.length() == 0)
            this.location = NOT_AVAILABLE;
        else
            this.location = location;
    }

    public void setExpiry(String expiry)
    {
        if(expiry.length() == 0)
            this.expiry = "Registered on " + NOT_AVAILABLE;
        else
            this.expiry = "Registered on " + expiry.replace('-',' ');
    }

    public String getNumber()
    {
        return number;
    }

    public String getName()
    {
        return name;
    }

    public String getFuel()
    {
        return fuel;
    }

    public String getCc()
    {
        return cc;
    }

    public String getEngine()
    {
        return engine;
    }

    public String getChassis()
    {
        return chassis;
    }

    public String getLocation()
    {
        return location;
    }

    public String getExpiry()
    {
        return expiry;
    }

    public String getOwner()
    {
        return owner;
    }

    public String cleanString(String attr)
    {
        return WordUtils.capitalizeFully(attr.replaceAll("[\\s],|,$|^,", ""));
    }
}