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
package com.stratumn.sdk.model.sdk;

import java.security.PrivateKey;
import java.util.Map;
import com.stratumn.sdk.TraceSdkException;

public class SdkConfig {

	/**
	 * The workflow id
	 */
	private String workflowId;

	/**
	 * The workflow config id
	 */
	private String configId;
	/**
	 * The account id
	 */
	private String accountId;
	/**
	 * The group id
	 */
	private String groupId;
	/**
	 * Map label to group id
	 */
	private Map<String, String> groupLabelToIdMap;

	/**
	 * The private key used for signing links
	 */
	private PrivateKey signingPrivateKey;

	public SdkConfig() {
	}

	public String getConfigId() {
		return configId;
	}

	public void setConfigId(String configId) {
		this.configId = configId;
	}

	public SdkConfig(String workflowId, String configId, String accountId, Map<String, String> groupLabelToIdMap,
			PrivateKey signingPrivateKey) {
		this.workflowId = workflowId;
		this.configId = configId;
		this.accountId = accountId;
		this.groupLabelToIdMap = groupLabelToIdMap;
		this.signingPrivateKey = signingPrivateKey;
	}

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getGroupId() throws TraceSdkException {
		return this.getGroupIdByLabel(null);
	}

	public String getGroupId(String groupLabel) throws TraceSdkException {
		return this.getGroupIdByLabel(groupLabel);
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	private String getGroupIdByLabel(String groupLabelParam) throws TraceSdkException {
		String resultGroupId = null;
		if (null != groupLabelToIdMap && 0 < groupLabelToIdMap.size()) {
			if (null == groupLabelParam) {
				if (groupLabelToIdMap.size() == 1) {
					// return the id of the only element
					resultGroupId = groupLabelToIdMap.get(groupLabelToIdMap.keySet().toArray()[0]);
				} else if (groupLabelToIdMap.size() > 1) {
					// Last check if groupId has been set manually
					if (null != this.groupId) {
						resultGroupId = this.groupId;
					} else {
						throw new TraceSdkException(
								"Multiple groups to select from, please specify the group label you wish to perform the action with.");
					}
				}
			} else {
				resultGroupId = groupLabelToIdMap.get(groupLabelParam);
			}
		}

		if (null == resultGroupId) {
			throw new TraceSdkException(
					"No group to select from. At least one group is required to perform an action.");
		}

		return resultGroupId;
	}

	public Map<String, String> getGroupLabelToIdMap() {
		return this.groupLabelToIdMap;
	}

	public void setGroupLabelToIdMap(Map<String, String> groupLabelToIdMap) {
		this.groupLabelToIdMap = groupLabelToIdMap;
	}

	public PrivateKey getSigningPrivateKey() {
		return signingPrivateKey;
	}

	public void setSigningPrivateKey(PrivateKey signingPrivateKey) {
		this.signingPrivateKey = signingPrivateKey;
	}

}
