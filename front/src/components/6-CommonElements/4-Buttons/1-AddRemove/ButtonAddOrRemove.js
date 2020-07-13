import React, { Component } from "react";

class AddButton extends Component {
  constructor(props) {
    super(props);
    this.state = { added: false };
  }

  handleAdd = event => {
    event.preventDefault();
    this.setState({ added: !this.state.added });
  };

  componentDidUpdate() {
    if (this.state.added !== this.props.added) {
      this.setState({ added: this.props.added });
    }
  }

  render() {
    return (
      <button
        onClick={this.handleAdd}
        className={
          this.state.added
            ? "btn btn-danger btn-sm"
            : "btn btn-secondary btn-sm"
        }
      >
        {this.state.added ? "Remove" : "Add"}
      </button>
    );
  }
}

export default AddButton;
