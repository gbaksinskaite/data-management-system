import React, { Component } from "react";

// FIX ID
// checkedStatus={function ()}
// id={id}
// ownerName={""}
class CheckBox extends Component {
  constructor(props) {
    super(props);
    this.state = { checked: false, owner: this.props.ownerName };
  }

  handleChangeCheckedState = event => {
    this.setState({ checked: event.target.checked });
    this.props.statusChange(event.target.checked, this.state.owner);
  };

  returnCheckedStatus = () => {
    this.props.checkedStatus(this.state.checked);
  };

  render() {
    return (
      <div>
        <input
          autoComplete="on"
          type="checkbox"
          id={this.props.id}
          onClick={this.handleChangeCheckedState}
        />
      </div>
    );
  }
}

export default CheckBox;