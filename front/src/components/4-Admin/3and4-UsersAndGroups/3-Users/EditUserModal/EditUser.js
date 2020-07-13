import React, { Component } from "react";
import Modal from "react-bootstrap/Modal";
import "./EditUser.css";
import UserInformation from "./FormComponents/1-UserInformation";
import Groups from "./FormComponents/2-Groups";
import axios from "axios";
import serverUrl from "./../../../../7-properties/1-URL";

class NewModal extends Component {
  constructor(props) {
    super(props);
    this.state = {
      firstName: "",
      lastName: "",
      password: "",
      role: "USER",
      allGroups: [],
      addedGroups: null,
      usernameExists: false
    };
  }

  loadingTable = () => {
    let loadingData = [];
    for (let i = 0; i < this.state.allGroups; i++) {
      loadingData.push({
        number: i,
        name: this.state.allGroups[i].name,
        addOrRemove: this.state.allGroups[i].addOrRemove
      });
    }
    return loadingData;
  };

  handleFirstNameChange = value => {
    this.setState({ firstName: value });
  };

  handleLastNameChange = value => {
    this.setState({ lastName: value });
  };

  handlePasswordChange = value => {
    this.setState({ password: value });
  };

  handleRoleChange = value => {
    this.setState({ role: value });
  };

  setAddedGroups = groupList => {
    this.setState({ addedGroups: groupList });
  };

  handleUpdateUser = event => {
    event.preventDefault();

    const { firstName, lastName, password, addedGroups, role } = this.state;

    let url = serverUrl + "user/update/" + this.props.ownerName;

    axios
      .post(url, {
        groupList: addedGroups,
        name: firstName,
        password: password,
        role: role,
        surname: lastName
      })
      .then(response => {
        this.props.onHide();
        this.props.reloadTable();
      })
      .catch(error => {
        console.log(error);
      });
  };

  initialStateUpdate = data => {
    this.setState({ ...data });
  };

  render() {
    return (
      <Modal
        show={this.props.show}
        onHide={this.props.onHide}
        size={"lg"}
        id="newUserModal"
      >
        <Modal.Header closeButton>
          <Modal.Title>Update {this.props.ownerName}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <form onSubmit={this.handleUpdateUser}>
            <UserInformation
              ownerName={this.props.ownerName}
              handleFirstNameChange={this.handleFirstNameChange}
              handleLastNameChange={this.handleLastNameChange}
              handleUsernameChange={this.handleUsernameChange}
              handlePasswordChange={this.handlePasswordChange}
              handleRoleChange={this.handleRoleChange}
              initialStateUpdate={this.initialStateUpdate}
              firstName={this.state.firstName}
              lastName={this.state.lastName}
              password={this.state.password}
              role={this.state.role}
            />

            <hr className="m-1" />

            <Groups
              tableData={this.state.allGroups}
              setUpGroups={this.setUpGroups}
              setAddedGroups={this.setAddedGroups}
              addedGroups={this.state.addedGroups}
            />

            <div className="form-group row d-flex justify-content-center">
              <div className="modal-footer ">
                <button
                  type="button"
                  className="btn btn-outline-dark"
                  onClick={this.props.onHide}
                >
                  Cancel
                </button>
                <button
                  type={"submit"}
                  className="btn btn-dark"
                  data-dismiss="modal"
                >
                  Update
                </button>
              </div>
            </div>
          </form>
        </Modal.Body>
      </Modal>
    );
  }
}

export default NewModal;
