var stompClient = null;
var connectedGlobal = false;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    connectedGlobal = connected;
}

function connect() {
    var socket = new SockJS('/chat-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/messages', function (messages) {
            showMessages(messages.body);
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
}

function sendMessage() {
    stompClient.send("/app/sendMessage", {}, JSON.stringify({'fromUserId': $("#fromId").val(), 'toUserId': $("#toId").val(), 'content': $("#content").val(), 'timeStamp': Date.now()}));
    $('#content').val("");
}

function fetchMessages() {
    stompClient.send("/app/getMessages", {}, JSON.stringify({'fromUserId': $("#fromId").val(), 'toUserId': $("#toId").val()}));
}

function showMessages(messages) {

    var clear= true;
    for (message of JSON.parse(messages)) {

       if((message.fromUserId != $("#fromId").val() && message.fromUserId != $("#toId").val())
        || (message.toUserId != $("#fromId").val() && message.toUserId != $("#toId").val())) {
           continue;
        }
        if (clear) {
            $("#messages").html("");
            clear = false;
        }

        var name = $("#fromName").val();
        var rightAlign = "<div class=\"col\" width=\"50%\"></div>\n";
        var leftAlign = "";
        var messageTextAlign = "text-end";
        if (message.fromUserId == $("#toId").val()) {
            name = $("#toName").val();
            leftAlign = "<div class=\"col\" width=\"50%\"></div>\n";
            rightAlign = "";
            messageTextAlign = "";
        }

        var timeDate = new Date(message.timeStamp);

        $("#messages").append(
                              "<div style=\"margin-right: 5px;margin-bottom: 15px;\">\n" +
                              "  <div class=\"row\">\n" +
                              rightAlign +
                              "    <div class=\"col\" width=\"50%\">\n" +
                              "        <div class=\"card shadow\">     \n" +
                              "            <p class=\"card-header fw-bold\">" + name + "</p>\n" +
                              "            <div class=\"card-body\">\n" +
                              "                <p class=\"" + messageTextAlign + " card-text fs-4\">" + message.content + " </p>\n" +
                              "                \n" +
                              "            </div>\n" +
                              "            <div class=\"card-footer text-end text-muted\" style=\"font-size: smaller;\">" + timeDate.toLocaleString() + "</div>\n" +
                              "        </div>\n" +
                              "    </div>\n" +
                              leftAlign +
                              "  </div>\n" +
                              " </div>\n" +
                              "</div>\n");
    }
}

$(function () {
    window.onbeforeunload = function(){
      disconnect();
    };

    $("#sendform").on('submit', function (e) {
        e.preventDefault();
    });

    if (!connectedGlobal) {
        connect();
    }

    $( "#send" ).click(function() { sendMessage(); });
    setInterval(function() { fetchMessages(); }, 1000);
    fetchMessages();
});