console.log("This is javascript");

const toggleSideBar=()=>{
if($(".sideBar").is(":visible")) {
    $(".sideBar").css("display","none");
    $(".content").css("margin-left","1%");
}else {
    $(".sideBar").css("display","block");
    $(".content").css("margin-left","20%");
}
};

const search=()=>{
//console.log("searching...");

let query=$("#search-input").val();
console.log(query);

if(query==''){
$(".search-result").hide();
}else{
console.log(query);

// sending request to server
let url=`http://localhost:8080/search/${query}`

fetch(url).then(response=>{
return response.json();
}).then((data) => {
//data
console.log(data);

let text=`<div class='list-group'>`

data.forEach((contact) =>{
text+=`<a href='/user/${contact.cId}/contact' class='list-group-item list-group-item-action'> ${contact.name} </a>`
});

text+=`</div>`;

$(".search-result").html(text);
$(".search-result").show();

});

//$(".search-result").show();
}
};

const donate = () => {

    let amount=$("#payment_field").val();
    console.log(amount);
    if(amount == '' || amount == null) {
        alert("amount is required");
        return;
    }

//    if(amount <= 0) {
//        alert("amount is more than 0");
//        return;
//    }

    // we will use ajax to send request to server to create order- jquery

    $.ajax({
    url: "/user/create_order",
    data: JSON.stringify({amount:amount, info: "order_request" }),
    contentType: "application/json",
    type: "POST",
    dataType: "json",
    success: function (response) {
    // invoked where success
    console.log(response);
    },
    error: function (error) {
    // invoked when error
    console.log(error);
    alert("something went wrong !!");
    },
    });
};
