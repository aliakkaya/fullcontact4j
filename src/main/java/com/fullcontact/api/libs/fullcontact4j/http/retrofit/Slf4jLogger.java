/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fullcontact.api.libs.fullcontact4j.http.retrofit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit.RestAdapter;

/**
 *
 * @author aliakkaya
 */
public class Slf4jLogger implements RestAdapter.Log {

    final Logger logger = LoggerFactory.getLogger(Slf4jLogger.class);
    
    public void log(String message) {
        logger.debug(message);
    }
}
