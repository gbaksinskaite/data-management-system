import React from "react";
import "./AttachFiles.css";

const UploadFile = props => {
  const handleFileUploaded = event => {
    props.handleFileAdd(event.target.files);
  };

  return (
    <div>
      <h3 className="d-flex justify-content-start">3. Attach files.</h3>
      <div className="input-group mb-3">
        <div className="custom-file">
          <input
            type="file"
            className="custom-file-input"
            id="documentFileUpload"
            aria-describedby="documentFileUpload"
            onChange={handleFileUploaded}
            accept="application/pdf"
            multiple
          />
          <label className="custom-file-label" htmlFor="documentFileUpload">
            Choose file
          </label>
        </div>
      </div>
    </div>
  );
};

export default UploadFile;
