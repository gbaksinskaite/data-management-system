package lt.vtmc.groups.dto;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lt.vtmc.docTypes.model.DocType;
import lt.vtmc.groups.model.Group;
import lt.vtmc.user.model.User;

public class GroupDetailsDTO {

	private String name;

	private String description;

	@JsonIgnore
	private List<User> userListArray;

	@JsonIgnore
	private List<DocType> docTypesToCreate;

	@JsonIgnore
	private List<DocType> docTypesToApprove;

	public GroupDetailsDTO(Group group) {
		super();
		this.name = group.getName();
		this.description = group.getDescription();
		this.userListArray = group.getUserList();
		this.docTypesToCreate = group.getDocTypesToCreate();
		this.docTypesToApprove = group.getDocTypesToApprove();
	}

	public void setUserListArray(List<User> userListArray) {
		this.userListArray = userListArray;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<User> getUserListArray() {
		return userListArray;
	}

	public String[] getUserList() {
		String[] userListNames = new String[userListArray.size()];
		for (int i = 0; i < userListArray.size(); i++) {
			userListNames[i] = userListArray.get(i).getUsername();
		}
		return userListNames;
	}

	public void setUserList(List<User> userList) {
		this.userListArray = userList;
	}

	public List<DocType> getDocTypesToCreate() {
		return docTypesToCreate;
	}

	public String[] getDocTypesToCreateNames() {
		String[] docTypesNames = new String[docTypesToCreate.size()];
		for (int i = 0; i < docTypesToCreate.size(); i++) {
			docTypesNames[i] = docTypesToCreate.get(i).getName();
		}
		return docTypesNames;
	}

	public void setDocTypesToCreate(List<DocType> docTypesToCreate) {
		this.docTypesToCreate = docTypesToCreate;
	}

	public List<DocType> getDocTypesToApprove() {
		return docTypesToApprove;
	}

	public String[] getDocTypesToApproveNames() {
		String[] docTypesNames = new String[docTypesToApprove.size()];
		for (int i = 0; i < docTypesToApprove.size(); i++) {
			docTypesNames[i] = docTypesToApprove.get(i).getName();
		}
		return docTypesNames;
	}

	public void setDocTypesToApprove(List<DocType> docTypesToApprove) {
		this.docTypesToApprove = docTypesToApprove;
	}

	@Override
	public String toString() {
		return "[name: " + name + ", description: " + getDescription() + ", users: " + Arrays.toString(getUserList())
				+ ", Can create: " + Arrays.toString(getDocTypesToCreateNames()) + ", Can approve: "
				+ Arrays.toString(getDocTypesToApproveNames()) + "]";
	}
}
