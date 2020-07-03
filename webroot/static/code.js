
const tbody = document.querySelector("#data");
var temp = "";
let servicesRequest = new Request('/service');
fetch(servicesRequest)
.then(function(response) { return response.json(); })
.then(function(serviceList) {
  serviceList.forEach(service => {
     console.log(service);
     tbody.appendChild(addRows(service));
  });
});


function addRows(data) {

 var tr = document.createElement("tr");
  var td1 = tr.insertCell();
  var td2 = tr.insertCell();
  var td3 = tr.insertCell();
  var td4 = tr.insertCell();
  var td5 = tr.insertCell();

  var urlName = document.createTextNode(data.name);
  td1.appendChild(urlName);

  var urlValue = document.createTextNode(data.url);
  td2.appendChild(urlValue);

  var urlStatus = document.createTextNode(data.status);
    td3.appendChild(urlStatus);

  var createdDate = document.createTextNode(new Date(data.created_date));
    td4.appendChild(createdDate);

   var delButton = document.createElement('button');
              delButton.type = 'button';
              delButton.classList = 'btn btn-danger btn-sm';
              delButton.setAttribute('service-name', data.url);
              delButton.setAttribute('onclick', 'removeService(this)')
              delButton.innerHTML = "Delete";
      td5.appendChild(delButton);
return tr;
}


const saveButton = document.querySelector('#post-service');
saveButton.onclick = evt => {

    let urlName = document.querySelector('#url-name').value;
    let urlValue = document.querySelector('#url-value').value;
    fetch('/service', {
    method: 'post',
    headers: {
    'Accept': 'application/json, text/plain, */*',
    'Content-Type': 'application/json'
    },
  body: JSON.stringify({name:urlName,url:urlValue})
}).then((response) =>  {
       if(response.status == 200){
          alert("Service URL added successfully");
       }
       else{
          alert("Invalid URL");
       }
    })
}

 function removeService(caller) {

      if (confirm("Proceed to delete ?")) {
            let serviceName  = caller.getAttribute('service-name');
            console.log(btoa(serviceName));
            let deleteRequest = new Request('/service/' + btoa(serviceName));
            fetch(deleteRequest, {
                method: 'delete'
            })
            .then(res=> location.reload());
       }
  }
