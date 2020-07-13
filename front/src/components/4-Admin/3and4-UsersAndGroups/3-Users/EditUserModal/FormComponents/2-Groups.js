import React, { Component } from "react";
import "./2-Groups.css";
import Table from "./../../../../../6-CommonElements/2-AdvancedTable/AdvancedTable";
import axios from "axios";
import serverUrl from "./../../../../../7-properties/1-URL";

class Groups extends Component {
  constructor(props) {
    super(props);
    this.state = {
      tableData: [],
      selectedGroupNames: [],
      initialSetupHappend: false
    };
  }

  columns = [{ dataField: "name", text: "Name", sort: true }];

  componentDidMount() {
    this.fetchGroupsData(0, 8, null, null, "");
  }

  componentDidUpdate() {
    if (!this.state.initialSetupHappend && this.props.addedGroups !== null) {
      this.setState({
        selectedGroupNames: this.props.addedGroups,
        initialSetupHappend: true
      });
    }
  }

  fetchGroupsData = (
    page,
    sizePerPage,
    sortField,
    order,
    searchValueString
  ) => {
    const pageData = {
      limit: sizePerPage,
      order: order,
      page: page,
      sortBy: sortField,
      searchValueString: searchValueString
    };

    axios
      .post(serverUrl + "groups", pageData)
      .then(response => {
        this.parseData(response.data.groupList);
        this.setState({ pagingData: response.data.pagingData });
      })
      .catch(error => {
        console.log(error);
      });
  };

  parseData = data => {
    this.setState({ allGroups: [] });
    let tempData = data.map((item, index) => {
      return {
        number: index + 1,
        name: item.name,
        description: item.description
      };
    });
    this.setState({ tableData: tempData });
  };

  handleRowSelect = (row, isSelect) => {
    const selectedGroupNames = this.state.selectedGroupNames;
    if (isSelect) {
      if (!selectedGroupNames.includes(row.name)) {
        selectedGroupNames.push(row.name);
      }
    } else {
      if (selectedGroupNames.includes(row.name)) {
        selectedGroupNames.splice(selectedGroupNames.indexOf(row.name), 1);
      }
    }
    this.setState({ selectedGroupNames: selectedGroupNames });
    this.props.setAddedGroups(selectedGroupNames);
  };

  setSelectedItems = () => {
    const { tableData, selectedGroupNames } = this.state;
    let selectedItemNumbersForTable = [];
    for (let index = 0; index < tableData.length; index++) {
      const element = tableData[index].name;
      if (selectedGroupNames.includes(element)) {
        selectedItemNumbersForTable.push(index + 1);
      }
    }
    return selectedItemNumbersForTable;
  };

  handleSelectAll = (isSelect, rows) => {
    rows.forEach(row => {
      setTimeout(() => {
        this.handleRowSelect(row, isSelect);
      }, 1);
    });
  };

  render() {
    return (
      <div className="mx-3">
        <div className="row d-flex justify-content-start">
          <h3 className="d-flex justify-content-start">
            2. Update user groups.
          </h3>
        </div>

        <Table
          id={"newUserGroups"}
          tableData={this.state.tableData}
          searchBarId={"currentGroupsSearchBar"}
          requestNewData={this.fetchGroupsData}
          pagingData={this.state.pagingData}
          columns={this.columns}
          selectType={"checkbox"}
          select={"true"}
          handleRowSelect={this.handleRowSelect}
          handleSelectAll={this.handleSelectAll}
          setSelectedItems={this.setSelectedItems}
        />
      </div>
    );
  }
}

export default Groups;
