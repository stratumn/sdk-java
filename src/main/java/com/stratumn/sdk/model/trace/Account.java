package com.stratumn.sdk.model.trace;
/**
 * A Stratumn Account object
 */
public class Account  
{ 
   private String account;

   public String getAccount()
   {
      return account;
   }

   public void setAccount(String account)
   {
      this.account = account;
   }

   public Account(String account)
   {
      super();
      this.account = account;
   }
   
}
