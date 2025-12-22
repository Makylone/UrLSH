package com.Makylone.UrLSH.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "url_mapping")
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @Column(nullable=false, columnDefinition="TEXT")
    private String originalUrl;

    @Column(unique=true)
    private String shortCode;

    @Column(nullable=false, columnDefinition="TIME")
    private LocalDateTime expireAt;

    public UrlMapping() {}
    
    public UrlMapping(String pOriginalUrl){
        this.originalUrl = pOriginalUrl;
    }

    public long getId(){
        return id;
    }

    public void setId(long id){
        this.id = id;
    }

    public String getOriginalUrl(){
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl){
        this.originalUrl = originalUrl;
    }

    public String getShortCode(){
        return shortCode;
    }

    public void setShortCode(String shortCode){
        this.shortCode = shortCode;
    }

    public LocalDateTime getExpireAt(){
        return expireAt;
    }

    public void setExpireAt(LocalDateTime expireDate){
        this.expireAt = expireDate;
    }
}
