/*
Copyright 2017 Stratumn SAS. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
  limitations under the License.
*/
package com.stratumn.sdk.model.client;

/**
 * The fetch options
 */
public class FetchOptions {
	/**
	 * The authentication to use (will not use the token provided via automatic
	 * login) defaults to undefined
	 */
	String authToken;

	/**
	 * Flag to bypass the automatic login mechanism defaults to false
	 */
	Boolean skipAuth;

	/**
	 * The retry count defaults to 1
	 */
	Integer retry=1;

	public FetchOptions( ) {
        
       this.skipAuth = false;
       this.retry = 1;
    }
	
	
	public FetchOptions(String authToken, Boolean skipAuth, Integer retry)  {
		 
		this.authToken = authToken;
		this.skipAuth = skipAuth;
		if (retry!=null )
		this.retry = retry;
	}

	public String getAuthToken() {
		return this.authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public Boolean getSkipAuth() {
		return this.skipAuth;
	}

	public void setSkipAuth(Boolean skipAuth) {
		this.skipAuth = skipAuth;
	}

	public Integer getRetry() {
		return this.retry;
	}

	public void setRetry(Integer retry) {
		this.retry = retry;
	}

}
