package com.icmd.ibuilder.auth;

import com.icmd.ibuilder.auth.exception.RegistrationNotSupportedException;


public abstract class AuthStrategy {

    boolean authenticated;

    public abstract boolean authenticate();


    public boolean isRegistrationSupported(){

        try {
            this.getClass().getDeclaredMethod("register", String.class, String.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     *  This method describes register method. By default register is not supported and therefore,
     *  you have to override this behaviour *i.e. if you want dynamic boolean behaviour in your strategy*
     * @param username
     * @param password
     * @throws RegistrationNotSupportedException if registration is not supported.
     */

    public boolean register(String username, String password){

        throw new RegistrationNotSupportedException();

    }


    public boolean process(){


        authenticated = authenticate();
        return authenticated;

    }


    public boolean isAuthenticated() {
        return authenticated;
    }
}
