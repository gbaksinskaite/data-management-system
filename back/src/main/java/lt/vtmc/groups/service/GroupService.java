package lt.vtmc.groups.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lt.vtmc.docTypes.dao.DocTypeRepository;
import lt.vtmc.docTypes.model.DocType;
import lt.vtmc.groups.dao.GroupRepository;
import lt.vtmc.groups.dto.GroupDetailsDTO;
import lt.vtmc.groups.model.Group;
import lt.vtmc.paging.PagingData;
import lt.vtmc.paging.PagingResponse;
import lt.vtmc.user.dao.UserRepository;
import lt.vtmc.user.model.User;

/**
 * Group service for creating and managing groups.
 * 
 * @author VStoncius
 *
 */
@Service
public class GroupService {

	@Autowired
	private GroupRepository groupRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private DocTypeRepository docTypesRepo;

	@Autowired
	PagingData pagingData;

	/**
	 * 
	 * This method finds groups from group repository.
	 *
	 * @param name of the group
	 * @return group entity type object
	 */
	public Group findGroupByName(String name) {
		return groupRepository.findGroupByName(name);
	}

	/**
	 * Method to create user groups.
	 *
	 * @param name of the group
	 * @param description of the group
	 * @return groupt entity type object
	 */
	@Transactional
	public Group createGroup(String name, String description) {
		Group newGroup = new Group();
		newGroup.setName(name);
		newGroup.setDescription(description);
		newGroup.setDocTypesToApprove(new ArrayList<DocType>());
		newGroup.setDocTypesToCreate(new ArrayList<DocType>());
		newGroup.setUserList(new ArrayList<User>());
		groupRepository.save(newGroup);
		return newGroup;

	}

	/**
	 * Method to add users to groups.
	 * 
	 * @param groupList of groups to add the user to
	 * @param username or the user added to groups
	 */
	@Transactional
	public void addUserToGroupByUsername(String[] groupList, String username) {
		User userToAdd = userRepository.findUserByUsername(username);
		for (int i = 0; i < groupList.length; i++) {
			Group groupToAddTo = groupRepository.findGroupByName(groupList[i]);
			List<User> tmpUserList = groupToAddTo.getUserList();
			List<Group> tmpGroupList = userToAdd.getGroupList();
			if (tmpUserList.contains(userToAdd) == false && tmpGroupList.contains(groupToAddTo) == false) {
				tmpGroupList.add(groupToAddTo);
				userToAdd.setGroupList(tmpGroupList);
				tmpUserList.add(userToAdd);
				groupToAddTo.setUserList(tmpUserList);
			}
		}
	}

	/**
	 * Method to remove users from groups.
	 * 
	 * @param groupList of groups to remove the user from
	 * @param username of the user to be removed from the groups
	 */
	@Transactional
	public void removeUserFromGroupByUsername(String[] groupList, String username) {
		User userToRemove = userRepository.findUserByUsername(username);
		for (int i = 0; i < groupList.length; i++) {
			Group groupToRemoveFrom = groupRepository.findGroupByName(groupList[i]);
			List<User> tmpUserList = groupToRemoveFrom.getUserList();
			List<Group> tmpGroupList = userToRemove.getGroupList();
			if (tmpUserList.contains(userToRemove) == true && tmpGroupList.contains(groupToRemoveFrom) == true) {
				tmpGroupList.remove(groupToRemoveFrom);
				userToRemove.setGroupList(tmpGroupList);
				tmpUserList.remove(userToRemove);
				groupToRemoveFrom.setUserList(tmpUserList);
			}
		}
	}

	/**
	 * Method to remove users from groups.
	 * 
	 * @param groupname of the group to remove users from
	 * @param userlist of users to remove from the group
	 */
	@Transactional
	public void removeUsersFromGroup(String groupname, String[] userlist) {
		Group groupToRemoveFrom = groupRepository.findGroupByName(groupname);
		for (int i = 0; i < userlist.length; i++) {
			User userToRemove = userRepository.findUserByUsername(userlist[i]);
			List<User> tmpUserList = groupToRemoveFrom.getUserList();
			List<Group> tmpGroupList = userToRemove.getGroupList();
			if (tmpUserList.contains(userToRemove) == true && tmpGroupList.contains(groupToRemoveFrom) == true) {
				tmpGroupList.add(groupToRemoveFrom);
				userToRemove.setGroupList(tmpGroupList);
				tmpUserList.add(userToRemove);
				groupToRemoveFrom.setUserList(tmpUserList);
			}
		}
	}
	/**
	 * Method to add users to groups.
	 * 
	 * @param groupname of the group to add users to
	 * @param userlist of users to add to the group
	 */
	@Transactional
	public void addUsersToGroup(String groupname, String[] userlist) {
		Group groupToAddTo = groupRepository.findGroupByName(groupname);
		for (int i = 0; i < userlist.length; i++) {
			User userToAdd = userRepository.findUserByUsername(userlist[i]);
			List<User> tmpUserList = groupToAddTo.getUserList();
			List<Group> tmpGroupList = userToAdd.getGroupList();
			if (tmpUserList.contains(userToAdd) == false && tmpGroupList.contains(groupToAddTo) == false) {
				tmpGroupList.add(groupToAddTo);
				userToAdd.setGroupList(tmpGroupList);
				tmpUserList.add(userToAdd);
				groupToAddTo.setUserList(tmpUserList);
			}
		}
	}
	/**
	 * Method to return all groups.
	 * 
	 * @param pagingData to set amount of items per page, search phrase and sorting
	 *                   order
	 * @return responseMap of groupDetailsDTO objects
	 */
	public Map<String, Object> retrieveAllGroups(PagingData pagingData) {
		Pageable firstPageable = pagingData.getPageable();
		Page<Group> grouplist = groupRepository.findLike(pagingData.getSearchValueString(), firstPageable);
		Map<String, Object> responseMap = new HashMap<String, Object>();
		responseMap.put("pagingData",
				new PagingResponse(grouplist.getNumber(), grouplist.getTotalElements(), grouplist.getSize()));
		responseMap.put("groupList", grouplist.getContent().stream().map(groupItem -> new GroupDetailsDTO(groupItem))
				.collect(Collectors.toList()));
		return responseMap;
	}

	/**
	 * Method to update user groups.
	 * 
	 * @param newGroupList of groups to add the user to
	 * @param username of the user to add to the group
	 */
	public void updateGroups(String[] newGroupList, String username) {
		List<Group> currentGroupList = new ArrayList<Group>();
		for (int i = 0; i < newGroupList.length; i++) {
			currentGroupList.add(groupRepository.findGroupByName(newGroupList[i]));
		}
		User tmpUser = userRepository.findUserByUsername(username);
		tmpUser.setGroupList(currentGroupList);
		userRepository.save(tmpUser);
	}

	/**
	 * Method to update group details.
	 *  
	 * @param newName for the group
	 * @param name of the group (current)
	 * @param description of the group
	 * @param newUserList to update
	 * @param docTypesToApprove full list of document types to approve
	 * @param docTypesToCreate full list of document types to create
	 */
	@Transactional
	public void updateGroupDetails(String newName, String name, String description, String[] newUserList,
			String[] docTypesToApprove, String[] docTypesToCreate) {
		Group groupToUpdate = groupRepository.findGroupByName(name);
		groupToUpdate.setDescription(description);
		groupToUpdate.setName(newName);
		List<User> currentUserList = groupToUpdate.getUserList();
		List<User> newList = new ArrayList<User>();
		for (String username : newUserList) {
			newList.add(userRepository.findUserByUsername(username));
		}

		for (User user : currentUserList) {
			if (!newList.contains(user)) {
				List<Group> tmpUserGroupList = user.getGroupList();
				tmpUserGroupList.remove(groupToUpdate);
			}
		}
		for (User user : newList) {
			List<Group> tmpUserGroupList = user.getGroupList();
			if (!tmpUserGroupList.contains(groupToUpdate)) {
				tmpUserGroupList.add(groupToUpdate);
			}
			userRepository.save(user);
		}
		groupRepository.save(groupToUpdate);
	}
	/**
	 * Method to add document types to existing group.
	 * 
	 * @param name of document type to add document types to
	 * @param docTypesToApprove list of document types to approve
	 * @param docTypesToCreate list of document types to create
	 */
	@Transactional
	public void addDocTypes(String name, String[] docTypesToApprove, String[] docTypesToCreate) {
		Group groupToAddTo = groupRepository.findGroupByName(name);
		List<DocType> docTypesToCreateList = new ArrayList<DocType>();
		for (int i = 0; i < docTypesToCreate.length; i++) {
			docTypesToCreateList.add(docTypesRepo.findDocTypeByName(docTypesToCreate[i]));
		}
		groupToAddTo.setDocTypesToCreate(docTypesToCreateList);
		List<DocType> docTypesToSignList = new ArrayList<DocType>();
		for (int i = 0; i < docTypesToApprove.length; i++) {
			docTypesToSignList.add(docTypesRepo.findDocTypeByName(docTypesToApprove[i]));
		}
		groupToAddTo.setDocTypesToApprove(docTypesToSignList);
		groupRepository.save(groupToAddTo);
	}

	public void deleteGroup(Group tmpGroup) {
		groupRepository.delete(tmpGroup);
	}

}
