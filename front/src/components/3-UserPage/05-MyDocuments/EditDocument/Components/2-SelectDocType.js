import React, { Component } from "react";
import axios from "axios";
import serverUrl from "./../../../../7-properties/1-URL";

class SelectType extends Component {
  constructor(props) {
    super(props);
    this.state = { tableData: [], selectedRow: undefined };
  }

  componentDidUpdate() {
    if (this.props.username !== "" && this.state.tableData.length === 0) {
      this.fetchUserDocTypes(0, 99999999, "name", "asc", "");
    }
  }

  dataFields = ["number", "type", "select"];
  columnNames = ["#", "Type", ""];

  fetchUserDocTypes = (
    page,
    sizePerPage,
    sortField,
    order,
    searchValueString
  ) => {
    if (this.props.username !== "") {
      const pagingData = {
        limit: sizePerPage,
        order: order,
        page: page,
        sortBy: sortField,
        searchValueString: searchValueString
      };
      axios
        .post(serverUrl + this.props.username + "/dtypescreate", pagingData)
        .then(response => {
          if (response.data.length !== 0 && response.data !== undefined) {
            this.processData(response.data.docTypes);
          }
        })
        .catch(error => {
          console.log(error);
        });
    }
  };

  processData = data => {
    var tableData = data.map((item, index) => {
      return {
        number: index + 1,
        type: item,
        select: (
          <button className="btn btn-secondary btn-sm" onClick={this.doNothing}>
            Select
          </button>
        )
      };
    });
    this.setState({ tableData: tableData });
  };

  doNothing = event => {
    event.preventDefault();
  };

  selectedRow = row => {
    this.props.handleDocTypeSelect(row.number);
  };

  options = () => {
    const { tableData } = this.state;
    if (tableData.length > 0) {
      return tableData.map((item, index) => {
        return (
          <option value={item.type} key={index}>
            {item.type}
          </option>
        );
      });
    }
  };

  handleSelectChange = event => {
    this.props.handleDocTypeSelect(event.target.value);
  };

  render() {
    return (
      <div>
        <h3 className="d-flex justify-content-start">
          2. Select document type.
        </h3>
        <select
          value={this.props.selected}
          id="selectDocType"
          name="selectDocType"
          onChange={this.handleSelectChange}
          className="form-control"
        >
          {this.options()}
        </select>
      </div>
    );
  }
}

export default SelectType;
