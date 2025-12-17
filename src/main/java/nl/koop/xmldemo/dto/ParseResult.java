package nl.koop.xmldemo.dto;

import java.util.ArrayList;
import java.util.List;

public class ParseResult {
    private String library;
    private int aantalArtikelen;
    private long tijdMs;
    private long geheugenKb;
    private boolean success;
    private String error;
    private List<String> artikelDetails = new ArrayList<>();

    public ParseResult(String library) {
        this.library = library;
        this.success = true;
    }

    // Getters en Setters
    public String getLibrary() {
        return library;
    }

    public void setLibrary(String library) {
        this.library = library;
    }

    public int getAantalArtikelen() {
        return aantalArtikelen;
    }

    public void setAantalArtikelen(int aantalArtikelen) {
        this.aantalArtikelen = aantalArtikelen;
    }

    public long getTijdMs() {
        return tijdMs;
    }

    public void setTijdMs(long tijdMs) {
        this.tijdMs = tijdMs;
    }

    public long getGeheugenKb() {
        return geheugenKb;
    }

    public void setGeheugenKb(long geheugenKb) {
        this.geheugenKb = geheugenKb;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
        this.success = false;
    }

    public List<String> getArtikelDetails() {
        return artikelDetails;
    }

    public void setArtikelDetails(List<String> artikelDetails) {
        this.artikelDetails = artikelDetails;
    }

    public void addArtikelDetail(String detail) {
        this.artikelDetails.add(detail);
    }
}