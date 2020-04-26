<%-- 
    Document   : index
    Created on : 27/mai/2019, 0:00:09
    Author     : pafgoncalves@ipc.pt
--%>
<%!
    String getAppURL(HttpServletRequest request) {
        String scheme = request.getScheme();
        int port = request.getServerPort();
        String appPath = scheme + "://" + request.getServerName();
        if( (!scheme.equalsIgnoreCase("http") || port != 80)
                && (!scheme.equalsIgnoreCase("https") || port != 443) ) {
            appPath += ":" + port;
        }
        appPath += request.getContextPath();
        return appPath;
    }
%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Arduino Simulator</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="icon" href="../favicon2.ico?v=2" type="image/x-icon"/>

        <link type="text/css" rel="stylesheet" href="../css/jquery-ui-1.12.1.css">
        <link type="text/css" rel="stylesheet" href="../css/draw2d/contextmenu.css" />

        <link type="text/css" rel="stylesheet" href="../css/bootstrap-4.3.1.min.css" id="bootstrap-css">
        <link type="text/css" rel="stylesheet" href="../css/font-awesome-4.7.0.min.css">

        <link type="text/css" rel="stylesheet" href="../css/prettify.css">
        
        
        
        <!--
            mudar isto para um ficheiro chamado style.css
        -->        
        <style type="text/css">

            body {
                padding: 5px;
                line-height: 1.1;
            }

            #componentsPalette {
                border: 1px solid black; 
                overflow-y: scroll;
                padding: 10px; 
                position: absolute; 
                min-width: 50px;
                left: 5px;
                top: 85px; 
                bottom: 5px;
            }

            #draw2dCanvasWrapper {
                overflow: scroll;
                border: 1px solid black;
                /*
                min-width:800px;
                min-height:500px;
                width: 100%;
                height: 100%;
                position: relative;
                */
                position: absolute;
                top: 85px;
                left: 70px;
                bottom: 5px;
                right: 5px;
            }

            #draw2dCanvas {
                width:6000px;
                height:5000px;
                -webkit-tap-highlight-color: rgba(0,0,0,0);
            }

            .component {
                max-width: 24px;
                padding-bottom: 10px;
                cursor: pointer;
            }

            #flash, #sram {
                white-space: nowrap;
            }

            #flash td:nth-child(1) {
                padding: 3px 10px;
                color: deeppink;
                cursor: pointer;
            }

            #flash td:nth-child(3), #sram td:nth-child(3) {
                color: cornflowerblue;
            }

            #sram td:nth-child(1) {
                color: deeppink;
            }

            #sram td {
                padding: 0px 10px;
                margin: 0px;
            }

            .flashSelected {
                border: 3px dashed green !important;
            }
            .flashSelected td:nth-child(1) {
                padding: 0px 7px !important;
            }
            .flashAddressSelected {
                background-color: yellow;
            }
            .flashAddressBreakpoint {
                background-color: orange;
            }

            li.L0, li.L1, li.L2, li.L3, li.L4,
            li.L5, li.L6, li.L7, li.L8, li.L9 {
                list-style-type: decimal-leading-zero;
            }
            /*
                        ol.linenums li :hover {
                            background-color: red;
                        }
            */
            ol.linenums {
                counter-reset: item;
            } 
            ol.linenums li {
                display: block;
            }
            ol.linenums li:before {
                content: counter(item)". ";
                counter-increment: item;
                color: black;
            }
            /*
            ol.linenums li:hover:before {
                background-color: red;
            }
            */
            
            .breakAccept {
                /* precisa do important porque o normal é ter cores alternadas no fundo */
                background-color: bisque !important;
            }
            ol.linenums li.breakAccept {
                cursor: pointer;
            }

            .breakAcceptSelected {
                background-color: orange !important;
            }
            ol.linenums li.breakAcceptSelected {
                cursor: pointer;
            }

            .currentLine {
                border: 3px dashed green;
            }
            
            label.btn {
                margin-bottom: 0px;
            }
        </style>
        
        
        
        <script src="../js/jquery-1.12.0.min.js"></script>
        <script src="../js/popper.min.js"></script>
        <script src="../js/bootstrap-4.3.1.min.js"></script>
        <script src="../js/prettify.js" defer></script>



        <script src="../js/draw2d/shifty.js"></script>
        <script src="../js/draw2d/raphael.js"></script>
        <script src="../js/jquery-ui-1.12.1.min.js"></script>
        <script src="../js/draw2d/jquery.autoresize.js"></script>
        <script src="../js/draw2d/jquery-touch_punch.js"></script>
        <script src="../js/draw2d/jquery.contextmenu.js"></script>
        <script src="../js/draw2d/rgbcolor.js"></script>
        <script src="../js/draw2d/StackBlur.js"></script>
        <script src="../js/draw2d/canvg.js"></script>
        <script src="../js/draw2d/Class.js"></script>
        <script src="../js/draw2d/json2.js"></script>
        <script src="../js/draw2d/pathfinding-browser.min.js"></script>
        <script src="../js/draw2d/draw2d.js"></script>

        <script src="components.js"></script>        

        <!--
            mudar isto para um ficheiro chamado app.js num namespace app
        -->
        <script type="text/javascript">


            var status = 0;
            var asm = null;
            var webSocket;
            var simulationId = '0000';

            var arduinoBoard = null;
            var canvas = null;

            var serialDialog;
            var flashDialog;
            var sramDialog;
            var debugDialog;
//            var uploadDialog;

            var firstStatus = true;

            var sourceCodeFiles = new Map();
            var sourceCodeLines = null;
            var breakpoints = null;

            var currentName = null;

            function getWebSocketURL(part) {
                let loc = window.location.pathname;
                let path = loc.substring(0, loc.lastIndexOf('/app/'));
                let url = ((window.location.protocol === "https:") ? "wss://" : "ws://") + window.location.host + path + "/ws/" + part;
                return url;
            }

            function connectWebSocket() {
                webSocket = new WebSocket(getWebSocketURL("client"));
                webSocket.onopen = function (message) {
                    console.log("WS connected");
                    webSocket.send(JSON.stringify({'cmd': 'status'}));
                    webSocket.send(JSON.stringify({'cmd': 'asm'}));
                };
                webSocket.onmessage = function (message) {
//                    console.log(message.data);
                    webSocketMessage(JSON.parse(message.data));
                };
                webSocket.onclose = function (message) {
                    connectWebSocket();
                };
                webSocket.onerror = function (message) {
                    webSocket.close();
                };
            }

            function webSocketMessage(data) {
//                console.log(data.type);
                if (data.type === 'exception') {
                    if (data.exception === 'BreakpointException' && asm === null) {
                        //webSocket.send(JSON.stringify({'cmd': 'asm'}));
                    }
                    flashDialog.dialog("open");
                    selectFlashAddress(data.address);
                    setTimeout(function (data) {
                        alert(data.exception + (data.message ? ': ' + data.message : '') + ' @ ' + data.address);
                    }, 100, data);
                } else if (data.type === 'pinChange') {
                    if (data.pin >= 2 && data.pin <= 13) {
                        if (data.value) {
                            pinOn(data.pin);
                        } else {
                            pinOff(data.pin);
                        }
                    }
                } else if (data.type === 'statusChange') {
                    simulationId = data.simulationId;
                    $('#simulation-id').text(data.simulationId);
                    if (data.status === 'running') {
                        status = 1;
                        $('#playpause i').removeClass('fa-play').addClass('fa-pause');
                        $('#playpause').addClass('active');
                        $('#step').attr("disabled", true);

                        //https://stackoverflow.com/questions/7916734/how-to-make-html-look-disabled
                        $('#flash').attr("disabled", true);
                        $('#sram').attr("disabled", true);

                        arduinoBoard.setPower(true);
                        showSpeed();
                    } else {
                        status = 0;
                        $('#playpause i').removeClass('fa-pause').addClass('fa-play');
                        $('#playpause').removeClass('active');
                        $('#step').attr("disabled", false);

                        $('#flash').attr("disabled", false);
                        $('#sram').attr("disabled", false);

                        selectFlashAddress(data.address);
                        selectSourceLine(data.address);

                        webSocket.send(JSON.stringify({'cmd': 'dataMemory'}));
                    }
                    $('#simulationName').text(parseFilename(data.sketchName));
                    if (firstStatus) {
//                        if (!data.sketchName || 0 === data.sketchName.length) {
                        let prevBtnStatus = window.sessionStorage.getItem("programmerActive");
                        if( prevBtnStatus===null || prevBtnStatus==='true' ) {
                            $('#btn-programmer').addClass("active");
                            connectProgrammerWebSocket();
                        }
                        firstStatus = false;
                    }
                } else if (data.type === 'speed') {
                    $('#speed').text(parseFloat(data.speed).toFixed(3));
                } else if (data.type === 'load') {
                    $('#simulationName').text(parseFilename(data.name));
                    asm = null;
                    sourceCodeFiles.clear();
                    sourceCodeLines = null;
                    webSocket.send(JSON.stringify({'cmd': 'asm'}));
                } else if (data.type === 'asm') {
                    let s = $('#flash');
                    s.empty();
                    asm = data.asm;
                    //limpar os ficheiros (acho que é aqui)
                    $('#source-code').empty();
                    $('#source-files').empty();
                    if (asm) {
                        for (let i = 0; i < asm.length; i++) {
                            let line = asm[i];
                            //o limit do split é diferente do do Java
                            let idx = line.indexOf(":");
                            let parts = [line.substring(0,idx),line.substring(idx+1)];
                            let addr = parts[0];
                            parts = parts[1].split("//");
                            let inst = parts[0];
                            let comment = parts.length > 1 ? ('//' + parts[1]) : '';

                            let tr = $("<tr></tr>");
                            tr.attr('id', 'flash-address-' + addr);
                            let td = $("<td>" + addr + ":</td>");
                            td.click(function (e) {
                                webSocket.send(JSON.stringify({
                                    'cmd': 'breakpoint',
                                    'address': $(e.target).parent().attr('id').substring(14),
                                }));
                            });
                            tr.append(td);
                            tr.append($("<td>" + inst + "</td>"));
                            tr.append($("<td>" + comment + "</td>"));
                            s.append(tr);
                        }
                    }
                } else if (data.type === 'dataMemory') {
                    let s = $('#sram');
                    if (data.memory.length > 100) {
                        s.empty();
                    }
                    for (let i = 0; i < data.memory.length; i++) {
                        let addr = data.memory[i].address;
                        let value = data.memory[i].value.toString(16);
                        value = value.toUpperCase();
                        if (value.length < 2)
                            value = '0' + value;
                        let name = addr;
                        let comment = '';
                        if (addr < 32) {
                            var r = addr;
                            if (r.length < 2)
                                r = '0' + r;
                            r = 'R' + r;
                            name = r;
                            if (addr === 26 || addr === 27)
                                comment = '//X';
                            if (addr === 28 || addr === 29)
                                comment = '//Y';
                            if (addr === 30 || addr === 31)
                                comment = '//Z';
                        } else {
                            let hexAddr = addr.toString(16);
                            hexAddr = hexAddr.toUpperCase();
                            while (hexAddr.length < 4)
                                hexAddr = '0' + hexAddr;
                            name = hexAddr;
                        }

                        let line = s.find('#sram-address-' + addr);
                        if (line.length > 0) {
                            let td = line.find('td:nth-child(2)');
                            let ant = td.text();
                            td.text(value);
                            if (ant !== value) {
                                line.css('background-color', 'orange');
                                setTimeout(function (ll) {
                                    ll.css('background-color', '');
                                }, 1500, line);
                            }
                        } else {
                            let tr = $("<tr></tr>");
                            tr.attr('id', 'sram-address-' + addr);
                            let td = $("<td>" + name + ":</td>");
                            tr.append(td);
                            tr.append($("<td>" + value + "</td>"));
                            tr.append($("<td>" + comment + "</td>"));
                            s.append(tr);
                        }
                    }
                } else if (data.type === 'breakpoints') {
                    breakpoints = data.breakpoints;
                    $('.flashAddressBreakpoint').removeClass('flashAddressBreakpoint');
                    $('.breakAcceptSelected').removeClass('breakAcceptSelected');
                    for (let i = 0; i < breakpoints.length; i++) {
                        let addr = ("000" + breakpoints[i].toString(16).toUpperCase()).substr(-4);
                        $('#flash-address-' + addr).children('td:first').addClass('flashAddressBreakpoint');
                        makeFlashAddressVisible(addr);
                        $("#source-code").find("[data-address='" + addr + "']").addClass('breakAcceptSelected');
                        makeSourceLineVisible(addr);
                    }
                } else if (data.type === 'step') {
                    selectFlashAddress(data.address);
                } else if (data.type === 'usart') {
                    var out = $('#serial-monitor-output');
                    var isScrolledToBottom = out.get(0).scrollHeight - out.get(0).clientHeight <= out.get(0).scrollTop + 1;
                    if (data.value === 13) {
                        out.append('<br>');
                    } else if (data.value >= 32 && data.value <= 126) {
                        out.append(String.fromCharCode(data.value));
                    }
                    // scroll to bottom if isScrolledToBottom
                    if (isScrolledToBottom) {
                        out.get(0).scrollTop = out.get(0).scrollHeight - out.get(0).clientHeight;
                    }
                } else if (data.type === 'sourceFile') {
                    let files = $('#source-files');
                    let filename = data.name;

//                    if (filename === 'leds.ino') {
//                        console.log(data.content);
//                    }
                    //https://stackoverflow.com/questions/5034781/js-regex-to-split-by-line
//                    let lines = data.content.replace(/\r\n|\n\r|\n|\r/g).split("\n");
                    let code = data.content;
                    if (code.startsWith("\n") || code.startsWith("\r")) {
                        code = ' ' + code;
                    }
                    code = code.replace(/</g, '&lt;').replace(/>/g, '&gt;');
                    code = "<pre style='overflow-y: hidden; overflow-x: scroll;'>" + PR.prettyPrintOne(code, 'cpp', true) + "</pre>";
                    let counter = 1;
                    code = code.replace(/(<li )(class=")/g, (match, el, cl) => {
                        let breakpoint = false;
                        let address = -1;
                        for (let i = 0; i < sourceCodeLines.length; i++) {
                            let l = sourceCodeLines[i];
                            if (l.line === counter && l.file === filename) {
                                address = l.address;
                                breakpoint = true;
                                break;
                            }
                        }
                        return el + " id='scline-" + (counter++) + "' " + (breakpoint ? "data-address='" + address + "' " : "") + cl + (breakpoint ? "breakAccept " : "");
                    });
                    sourceCodeFiles.set(filename, code);

                    let o = files.find('option:contains("' + filename + '")');
                    if (o.length > 0) {
                        o.text(filename);
                    } else {
                        files.append($("<option></option>").attr("value", filename).text(filename));
                    }

                    let curr = $("#source-files option:selected").val();
                    if (curr === filename) {
                        $('#source-code').empty().append(sourceCodeFiles.get(filename));
                        $("#source-files").trigger("change");
                    }
                } else if (data.type === 'sourceLines') {
                    console.log(data.lines);
                    sourceCodeLines = data.lines;
                } else if(data.type === 'loadCircuit') {
                    currentName = data.name;
                    $('#projectName').text(currentName);
                    if( !data.circuit ) {
                        return;
                    }
                    let reader = new draw2d.io.json.Reader();
                    canvas.clear();
                    reader.unmarshal(canvas, data.circuit);
                    registerArduinoBoard();
                } else if(data.type === 'simulationsList') {
                    if( data.list ) {
                        let list = $('#btn-open-list');
                        list.empty();
                        for(let i = 0; i < data.list.length; i++) {
                            let obj = data.list[i];
                            let menu = $('<a class="dropdown-item" data-id='+obj.id+' style="cursor: pointer;">'+obj.name+'</a>');
                            menu.click(function (e) {
                                webSocket.send(JSON.stringify({
                                    'cmd': 'open',
                                    'id': $(e.target).data('id'),
                                }));
                            });
                            
                            list.append(menu);
                        }
                    }
                }
            }

            function selectFlashAddress(address) {
                $('#flash tr').removeClass('flashSelected');
                let addr = ("000" + address.toString(16).toUpperCase()).substr(-4);
                let tr = $('#flash-address-' + addr);
                tr.addClass('flashSelected');
                makeFlashAddressVisible(addr);
                selectSourceLine(addr);
            }

            function makeFlashAddressVisible(addr) {
                let tr = $('#flash-address-' + addr);
                try {
                    let elementTop = tr.position().top;
                    let elementBottom = elementTop + tr.outerHeight();

                    let viewportTop = $('#flash-dialog').scrollTop();
                    let viewportBottom = viewportTop + $('#flash-dialog').height();

                    if (elementBottom > viewportBottom || elementTop < viewportTop) {
                        let diffTop = elementTop - viewportTop;
                        tr.get(0).scrollIntoView({block: "start", behavior: Math.abs(diffTop) < 400 ? "smooth" : "instant"});
                    }
                } catch (err) {
                }
            }

            function selectSourceLine(address) {
                makeSourceLineVisible(address);
                let li = $("#source-code").find("[data-address='" + address + "']");
                if( li ) {
                    li.addClass('currentLine');
                }
            }

            function makeSourceLineVisible(address) {
                if (!sourceCodeLines) {
                    $('.currentLine').removeClass('currentLine');
                    return;
                }
                for (let i = 0; i < sourceCodeLines.length; i++) {
                    let line = sourceCodeLines[i];
                    if (line.address === address) {
                        $('.currentLine').removeClass('currentLine');
                        //trocar o ficheiro caso seja diferente do actualmente mostrado
                        if( $('#source-files').val() !== line.file ) {
                            $('#source-files').val(line.file);
                            $("#source-files").trigger("change");
                        }
                        let li = $("#source-code").find("[data-address='" + address + "']");
                        try {
                            let elementTop = li.position().top;
                            let elementBottom = elementTop + li.outerHeight();

                            let viewportTop = $('#debug-dialog').scrollTop();
                            let viewportBottom = viewportTop + $('#debug-dialog').height();

                            if (elementBottom > viewportBottom || elementTop < viewportTop) {
                                let diffTop = elementTop - viewportTop;
                                li.get(0).scrollIntoView({block: "start", behavior: Math.abs(diffTop) < 400 ? "smooth" : "instant"});
                            }
                        } catch (err) {
                            console.log(err);
                        }

                        break;
                    }
                }

            }

            function websocketPing() {
                setTimeout(function () {
                    websocketPing();
                }, 5000);
                if (webSocket) {
                    try {
                        webSocket.send(JSON.stringify({'cmd': 'ping'}));
                    } catch (err) {
                    }
                }
            }

            function parseFilename(filename) {
                if( !filename ) {
                    return '';
                } 
                return (filename.endsWith('.ino.hex') || filename.endsWith('.ino.elf')) ? filename.substring(0, filename.length - 8) : filename;
            }

            function showSpeed() {
                webSocket.send(JSON.stringify({'cmd': 'speed'}));
                if (status == 1) {
                    setTimeout(function () {
                        showSpeed();
                    }, 5000);
                }
            }

            function pinOn(id) {
                arduinoBoard.setPinValue(id, 5);
            }
            function pinOff(id) {
                arduinoBoard.setPinValue(id, 0);
            }

            function checkWidth() {
                var $el = $('#info');
                var bottom = /*$el.position().top +*/ $el.offset().top + $el.outerHeight(true) + 2;
                //console.log(bottom);
                $('#componentsPalette').css('top', bottom);
                $('#draw2dCanvasWrapper').css('top', bottom);
            }


            function registerArduinoBoard() {
                canvas.getFigures().each(function (i, figure) {
                    if (figure.NAME !== null && figure.NAME.startsWith('arduino.board')) {
                        arduinoBoard = figure;

                        arduinoBoard.onPinWrite = function (pin, value) {
                            //console.log("pinWrite html", pin, value);
                            webSocket.send(JSON.stringify({'cmd': 'pinChange', 'pin': pin, 'value': value}));
                        };
                    }
                });
            }

            function getProgrammerURL() {
                return getWebSocketURL('load');
            }


            function dialogOptions(name) {
                let opts = {};
                if (window.sessionStorage.getItem(name) === 'true') {
                    opts.autoOpen = true;
                }
                if (window.sessionStorage.getItem(name + ".width")) {
                    opts.width = window.sessionStorage.getItem(name + ".width");
                }
                if (window.sessionStorage.getItem(name + ".height")) {
                    opts.height = window.sessionStorage.getItem(name + ".height");
                }
                if (window.sessionStorage.getItem(name + ".left") && window.sessionStorage.getItem(name + ".top")) {
                    opts.position = {};
                    opts.position.my = 'left top';
                    opts.position.at = 'left+' + window.sessionStorage.getItem(name + ".left") + ' top+' + window.sessionStorage.getItem(name + ".top");
                    opts.position.of = window;
                }
                return opts;
            }

            function saveDialogPosition(name, ui) {
                //https://stackoverflow.com/questions/38625158/jquery-ui-dialog-save-position-via-php-only-works-with-draggable-not-dialo
                window.sessionStorage.setItem(name + ".top", ui.offset.top);
                window.sessionStorage.setItem(name + ".left", ui.offset.left);
            }

            function saveDialogSize(name, ui) {
                window.sessionStorage.setItem(name + ".width", ui.size.width);
                window.sessionStorage.setItem(name + ".height", ui.size.height);
            }

            var programmerWebSocket;
            var programmerWebSocketPorts = [8085, 8086, 8087];
            var programmerWebSocketPortIndex = 0;
            function connectProgrammerWebSocket() {
//                console.log("connect programmer");
                try {
                    if (programmerWebSocketPortIndex >= programmerWebSocketPorts.length) {
                        programmerWebSocketPortIndex = 0;
                    }
                    var port = programmerWebSocketPorts[programmerWebSocketPortIndex];
                    programmerWebSocketPortIndex++;
                    //adicionar timeout
                    //https://stackoverflow.com/questions/51526711/how-do-i-change-the-timeout-for-an-opening-a-websocket
                    //mudei de 'localhost' para '127.0.0.1' porque no caso da máquina ter IPv6
                    //demora demasiado tempo a dar timeout
                    programmerWebSocket = new WebSocket("ws://127.0.0.1:" + port + "/arduino");
//                    console.log("connect programmer ok");
                } catch (err) {
                    console.log(err);
                }
                programmerWebSocket.onopen = function (message) {
//                    console.log("programmer WS connected");
                    programmerWebSocket.send(JSON.stringify({
                        'cmd': 'webClient',
                        'address': getProgrammerURL(),
                        'simulationId': simulationId,
                    }));
                    //para desactivar o pulling do programador após a primeira ligação
                    //$('#btn-programmer').removeClass("active");
                };
                programmerWebSocket.onmessage = function (message) {
                    console.log(message.data);
                };
                programmerWebSocket.onclose = function (message) {
//                    console.log("programmer WS onclose");
                    setTimeout(function () {
//                        if ($('#prog').is(':checked')) {
                        if ($('#btn-programmer').hasClass("active")) {
                            connectProgrammerWebSocket();
                        }
                    }, programmerWebSocketPortIndex === programmerWebSocketPorts.length ? 500 : 1);
                };
                programmerWebSocket.onerror = function (message) {
                    programmerWebSocket.close();
                };
            }


            $(document).ready(function () {

                //para os botoes não ficarem com o focus
                $('.btn').mousedown(function (e) {
                    e.preventDefault();
                });
                
                //activar as tooltips dos botões
                $('[data-toggle="tooltip"]').tooltip({placement: "right", delay: {"show": 2500, "hide": 100}});
                
                let programmerUrl = getProgrammerURL();
                $('#programmer-url').html($('<a>', {text: programmerUrl, href: programmerUrl}));

                //manter sessão http
                setInterval(function () {
                    $.get("ping.html?time=" + (new Date().getTime()), function (data) {});
                }, 60000);


                connectWebSocket();
                websocketPing();
                

                $('#start').click(function (e) {
                    webSocket.send(JSON.stringify({'cmd': 'start'}));
                });
                $('#playpause').click(function (e) {
                    if (status == 0) {
                        webSocket.send(JSON.stringify({'cmd': 'resume'}));
//                        $('#playpause').prop('value', "Pause");
                    } else {
                        webSocket.send(JSON.stringify({'cmd': 'stop'}));
//                        $('#playpause').prop('value', "Play");
                        if (asm == null) {
                            webSocket.send(JSON.stringify({'cmd': 'asm'}));
                        }
                    }
                });
                $('#stop').click(function (e) {
                    webSocket.send(JSON.stringify({'cmd': 'stop'}));
                    arduinoBoard.setPower(false);
                    for (var i = 8; i <= 13; i++) {
                        pinOff(i);
                    }
                });
                $('#step').click(function (e) {
                    webSocket.send(JSON.stringify({'cmd': 'step'}));
                });

                $('#prog').change(function (e) {
                    if ($(e.target).get(0).checked) {
                        connectProgrammerWebSocket();
                    }
                });
                $('#btn-programmer').click(function (e) {
                    $('#btn-programmer').toggleClass("active");
                    if ($('#btn-programmer').hasClass("active")) {
                        connectProgrammerWebSocket();
                        window.sessionStorage.setItem("programmerActive", true);
                    } else {
                        window.sessionStorage.setItem("programmerActive", false);
                    }
                });

                $('#btn-zoom-in').click(function (e) {
                    canvas.setZoom(canvas.getZoom() * 0.7, true);
                });
                $('#btn-zoom-reset').click(function (e) {
                    canvas.setZoom(1, true);
                });
                $('#btn-zoom-out').click(function (e) {
                    canvas.setZoom(canvas.getZoom() * 1.3, true);
                });

                $('#btn-undo').click(function (e) {
                    canvas.getCommandStack().undo();
                });
                $('#btn-redo').click(function (e) {
                    canvas.getCommandStack().redo();
                });


                $('#btn-save').click(function (e) {
                    let pinValues = [];
                    if( arduinoBoard ) {
                        for (let i = 8; i <= 13; i++) {
                            pinValues[i] = arduinoBoard.getPinValue(i);
                            arduinoBoard.setPinValue(i, null);
                        }
                    }
                    let writer = new draw2d.io.json.Writer();
                    writer.marshal(canvas, function (json) {
                        if( arduinoBoard ) {
                            for (let i = 8; i <= 13; i++) {
                                arduinoBoard.setPinValue(i,pinValues[i]);
                            }
                        }
                        // convert the json object into string representation
                        let jsonTxt = JSON.stringify(json, null, 2);
//                        window.localStorage.setItem("savedCircuit", jsonTxt);
//                        console.log(jsonTxt);
                        
                        console.log(currentName);

                        if (!currentName) {
                            $("#dialog-ask-filename").dialog({
                                autoOpen: false,
                                modal: true,
                                buttons: {
                                    "Ok": function () {
                                        let text2 = $("#dialog-ask-filename-filename");
                                        //Do your code here
                                        currentName = text2.val();
                                        $(this).dialog("close");
                                        if( currentName ) {
                                            webSocket.send(JSON.stringify({'cmd': 'save', 'name': currentName, 'circuit': jsonTxt}));
                                        }
                                    },
                                    "Cancel": function () {
                                        $(this).dialog("close");
                                    }
                                }
                            }).dialog("open");
                        } else {
                            webSocket.send(JSON.stringify({'cmd': 'save', 'name': currentName, 'circuit': jsonTxt}));
                        }
                    });
                });
                $('#btn-new').click(function (e) {
                    /*
                    let saved = window.localStorage.getItem("savedCircuit");
                    if (saved !== null) {
                        let reader = new draw2d.io.json.Reader();
                        canvas.clear();
                        reader.unmarshal(canvas, saved);

                        registerArduinoBoard();
                    }*/
                    canvas.clear();
                    currentName = null;
                    simulationId = '0000';
                    $('#simulationName').text('');
                    $('#projectName').text('');
                    //criar uma nova simulação
                    webSocket.send(JSON.stringify({'cmd': 'new'}));
                });
                
                $('#btn-delete').click(function (e) {
                    canvas.clear();
                    currentName = null;
                    simulationId = '0000';
                    $('#simulationName').text('');
                    $('#projectName').text('');
                    //apagar e criar uma nova simulação
                    webSocket.send(JSON.stringify({'cmd': 'delete'}));
                });
                
                $('#btn-fav1').click(function (e) {
                    $.getJSON("default_circuit.json", function (data) {
                        let reader = new draw2d.io.json.Reader();
                        canvas.clear();
                        reader.unmarshal(canvas, data);

                        registerArduinoBoard();
                    });
                });
                $('#btn-fav2').click(function (e) {
                    $.getJSON("default_circuit2.json", function (data) {
                        let reader = new draw2d.io.json.Reader();
                        canvas.clear();
                        reader.unmarshal(canvas, data);

                        registerArduinoBoard();
                    });
                });
                $('#btn-fav3').click(function (e) {
                    $.getJSON("default_circuit3.json", function (data) {
                        let reader = new draw2d.io.json.Reader();
                        canvas.clear();
                        reader.unmarshal(canvas, data);

                        registerArduinoBoard();
                    });
                });

                $(window).resize(function () {
                    checkWidth();
                });
                //checkWidth();

                var configDialog = $("#config-dialog").dialog({
                    autoOpen: false,
                    modal: true,
                    height: 450,
                    width: 800,
                    buttons: {
                        Cancel: function () {
                            configDialog.dialog("close");
                        }
                    },
                });
                $('#btn-config').click(function (e) {
                    configDialog.dialog("open");
                });

                $('#btn-logout').click(function (e) {
                    window.location.href = "../logout";
                });


                serialDialog = $("#serial-monitor").dialog(jQuery.extend({
                    autoOpen: false,
                    width: 500,
                    height: 300,
                    close: function () {
                        $('#btn-serial').removeClass('active');
                        window.sessionStorage.setItem("serialDialog", false);
                    },
                    dragStop: function (event, ui) {
                        saveDialogPosition('serialDialog', ui);
                    },
                    resizeStop: function (event, ui) {
                        saveDialogSize('serialDialog', ui);
                    }
                }, dialogOptions('serialDialog')));

                $('#btn-serial').click(function (e) {
                    if ($('#btn-serial').hasClass('active')) {
                        $('#btn-serial').removeClass('active');
                        serialDialog.dialog("close");
                    } else {
                        $('#btn-serial').addClass('active');
                        serialDialog.dialog("open");
                        window.sessionStorage.setItem("serialDialog", true);
                        let out = $('#serial-monitor-output');
                        out.get(0).scrollTop = out.get(0).scrollHeight - out.get(0).clientHeight;
                    }
                });
                $('#btn-serial-monitor-clear').click(function (e) {
                    $('#serial-monitor-output').empty();
                });

                $('#serial-monitor-input').on('keypress', function (e) {
                    if (e.which === 13) {
                        webSocket.send(JSON.stringify({
                            'cmd': 'usart',
                            'value': $('#serial-monitor-input').val() + "\n",
                        }));
                        $('#serial-monitor-input').val('');
                    }
                });


                flashDialog = $("#flash-dialog").dialog(jQuery.extend({
                    autoOpen: false,
                    width: 500,
                    height: 300,
                    close: function () {
                        $('#btn-flash').removeClass('active');
                        window.sessionStorage.setItem("flashDialog", false);
                    },
                    dragStop: function (event, ui) {
                        saveDialogPosition('flashDialog', ui);
                    },
                    resizeStop: function (event, ui) {
                        saveDialogSize('flashDialog', ui);
                    }
                }, dialogOptions('flashDialog')));

                $('#btn-flash').click(function (e) {
                    if ($('#btn-flash').hasClass('active')) {
                        $('#btn-flash').removeClass('active');
                        flashDialog.dialog("close");
                    } else {
                        $('#btn-flash').addClass('active');
                        flashDialog.dialog("open");
                        window.sessionStorage.setItem("flashDialog", true);
                    }
                });



                sramDialog = $("#sram-dialog").dialog(jQuery.extend({
                    autoOpen: false,
                    width: 500,
                    height: 300,
                    close: function () {
                        $('#btn-sram').removeClass('active');
                        window.sessionStorage.setItem("sramDialog", false);
                    },
                    dragStop: function (event, ui) {
                        saveDialogPosition('sramDialog', ui);
                    },
                    resizeStop: function (event, ui) {
                        saveDialogSize('sramDialog', ui);
                    }
                }, dialogOptions('sramDialog')));

                $('#btn-sram').click(function (e) {
                    if ($('#btn-sram').hasClass('active')) {
                        $('#btn-sram').removeClass('active');
                        sramDialog.dialog("close");
                    } else {
                        $('#btn-sram').addClass('active');
                        sramDialog.dialog("open");
                        window.sessionStorage.setItem("sramDialog", true);
                    }
                });


                debugDialog = $("#debug-dialog").dialog(jQuery.extend({
                    autoOpen: false,
                    width: 500,
                    height: 300,
                    close: function () {
                        $('#btn-debug').removeClass('active');
                        window.sessionStorage.setItem("debugDialog", false);
                    },
                    dragStop: function (event, ui) {
                        saveDialogPosition('debugDialog', ui);
                    },
                    resizeStop: function (event, ui) {
                        saveDialogSize('debugDialog', ui);
                    }
                }, dialogOptions('debugDialog')));
                $('#btn-debug').click(function (e) {
                    if ($('#btn-debug').hasClass('active')) {
                        $('#btn-debug').removeClass('active');
                        debugDialog.dialog("close");
                    } else {
                        $('#btn-debug').addClass('active');
                        debugDialog.dialog("open");
                        window.sessionStorage.setItem("debugDialog", true);
                    }
                });
                $('#source-files').change(function () {
                    let filename = $("#source-files option:selected").val();
                    if (sourceCodeFiles.has(filename)) {
//                        console.log(sourceCodeFiles.get(filename));
                        $('#source-code').empty().append(sourceCodeFiles.get(filename));
                        //TODO: tem de se assinalar os breakpoints 
                        $('ol.linenums').on('click', 'li', function (e) {
                            if (e.target.nodeName === 'LI') {
                                let li = $(e.target);
                                if (li.hasClass("breakAccept")) {
                                    let address = li.data("address");

                                    webSocket.send(JSON.stringify({
                                        'cmd': 'breakpoint',
                                        'address': address,
                                    }));

                                }
                            }
                        });
                    }
                });
/*
                uploadDialog = $("#dialog-upload-file").dialog(jQuery.extend({
                    autoOpen: false,
                    width: 500,
                    height: 300,
                    close: function () {
                    },
                    dragStop: function (event, ui) {
                        saveDialogPosition('uploadDialog', ui);
                    },
                    resizeStop: function (event, ui) {
                        saveDialogSize('uploadDialog', ui);
                    }
                }, dialogOptions('uploadDialog')));
                $('#btn-upload').click(function (e) {
                    uploadDialog.dialog("open");
                });
                $('#dialog-upload-file-file').change(function(e){
                    uploadDialog.dialog("close");
                    let files = e.target.files;
                    if( files && files.length>0 ) {
                        let file = files[0];
                        let reader = new FileReader();
                        reader.onload = function(readerEvt) {
                            let binaryString = readerEvt.target.result;
                            $("#dialog-upload-file-file").val('');
                            webSocket.send(JSON.stringify({
                                'cmd': 'load',
                                'fileName': file.name,
                                'content': btoa(binaryString),
                            }));
                        };
                        reader.readAsBinaryString(file);
                    }
                });
*/                
                $('#file-upload').change(function(e){
                    let files = e.target.files;
                    if( files && files.length>0 ) {
                        let file = files[0];
                        let reader = new FileReader();
                        reader.onload = function(readerEvt) {
                            let binaryString = readerEvt.target.result;
                            $("#file-upload").val('');
                            webSocket.send(JSON.stringify({
                                'cmd': 'load',
                                'fileName': file.name,
                                'content': btoa(binaryString),
                            }));
                        };
                        reader.readAsBinaryString(file);
                    }
                });







                canvas = new draw2d.Canvas("draw2dCanvas");
                canvas.setScrollArea($(window));

                let router = new draw2d.layout.connection.InteractiveManhattanConnectionRouter();
//                let router = new draw2d.layout.connection.CircuitConnectionRouter();
                canvas.installEditPolicy(new draw2d.policy.connection.ComposedConnectionCreatePolicy(// Responsible for connection creation....
                        [
                            // create a connection via Drag&Drop of ports
                            //
                            new draw2d.policy.connection.DragConnectionCreatePolicy({
                                createConnection: function () {
                                    return new draw2d.Connection({
                                        radius: 2,
                                        stroke: 2,
                                        router: router});
                                }
                            }),
                            // or via click and point
                            //
                            new draw2d.policy.connection.OrthogonalConnectionCreatePolicy({
                                createConnection: function () {
                                    return new draw2d.Connection({
                                        radius: 2,
                                        stroke: 2,
                                        router: router});
                                }
                            })
                        ])
                        );

                canvas.onDrop = function (droppedDomNode, x, y, shiftKey, ctrlKey) {
                    let type = $(droppedDomNode).data("shape");
                    if (type.startsWith("arduino.board.")) {
                        let exists = false;
                        canvas.getFigures().each(function (i, f) {
                            if (f.NAME.startsWith("arduino.board.")) {
                                exists = true;
                            }
                        });
                        if (exists) {
                            alert("Can't place more than one Arduino in the circuit!");
                            return;
                        }
                    }
                    let figure = eval("new " + type + "();"); // jshint ignore:line
                    // create a command for the undo/redo support
                    let command = new draw2d.command.CommandAdd(this, figure, x - 30, y);
                    this.getCommandStack().execute(command);
                    if (type.startsWith("arduino.board.")) {
                        registerArduinoBoard();
                    }
                }

            });


        </script>
    </head>
    <body>

        <!-- dialogs -->
        <div id="serial-monitor" title="Serial monitor" style="height: 100%; width: 100%;">
            <table style="height: 100%; width: 100%;">
                <tr><td colspan="2">
                        <div id="serial-monitor-output" style="width: 100%; height: 100%; min-height: 100px; border: 1px solid black; overflow-y: scroll;"></div>
                    </td></tr>
                <tr style="height: 1px;">
                    <td>
                        <input type="text" id="serial-monitor-input" style="width: 100%;">
                    </td>
                    <td style="width: 1px;">
                        <button id="btn-serial-monitor-clear" ><i class="fa fa-fw fa-eraser"></i></button>
                    </td>
                </tr>
            </table>
        </div>

        <div id="config-dialog" title="Config" style="height: 100%; width: 100%;">
            Board URL: <a href="<%=getAppURL(request)%>/package/package_arduinosim_index.json"><%=getAppURL(request)%>/package/package_arduinosim_index.json</a>
            <hr>
            Programmer URL: <span id="programmer-url"></span><br>
            Simulation ID: <span id="simulation-id"></span><br>
        </div>

        <div id="flash-dialog" title="FLASH" style="height: 100%; width: 100%;">
            <table id="flash" border="0">
            </table>
        </div>

        <div id="sram-dialog" title="SRAM" style="height: 100%; width: 100%;">
            <table id="sram" border="0">
            </table>
        </div>

        <div id="debug-dialog" title="Debug" style="height: 100%; width: 100%;">
            <table style="height: 100%; width: 100%;">
                <tr><td colspan="2">
                        <div id="source-code" style="width: 100%; height: 100%; min-height: 100px; border: 1px solid black; overflow-y: scroll;"></div>
                    </td></tr>
                <tr style="height: 1px;">
                    <td>
                        <select id="source-files" style="width: 100%;"></select>
                    </td>
                    <td style="width: 1px;">
                        <button id="btn-xxxxx" ><i class="fa fa-fw fa-eraser"></i></button>
                    </td>
                </tr>
            </table>
        </div>

        <div id="dialog-ask-filename" style="display: none;">
            <form>
                <label for="name">Name</label>
                <input type="text" name="name" id="dialog-ask-filename-filename" class="text ui-widget-content ui-corner-all" onkeydown="return event.key != 'Enter';" />
            </form>
        </div>
<!--
        <div id="dialog-upload-file" style="display: none;">
            <form>
                <label for="file-upload">Arduino binary file (hex or elf)</label>
                <input type="file" name="file-upload1" accept=".hex,.elf" id="dialog-upload-file-file1" class="text ui-widget-content ui-corner-all" />
            </form>
        </div>
-->

        <!-- toolbars -->

        <div class="btn-toolbar" role="toolbar">
            <div class="btn-group mr-2" role="group">
                <button class="btn btn-outline-dark" id="btn-new" data-toggle="tooltip" data-placement="bottom" data-delay="2000" title="New"><i class="fa fa-fw fa-file-o"></i></button>
                <div class="btn-group" role="group">
                    <button class="btn btn-outline-dark dropdown-toggle" id="btn-open" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" title="Open"><i class="fa fa-fw fa-folder-open-o"></i></button>
                    <div class="dropdown-menu" aria-labelledby="btn-open" id="btn-open-list">
                    </div>
                </div>
                <button class="btn btn-outline-dark" id="btn-save" data-toggle="tooltip" data-placement="bottom" title="Save the current circuit"><i class="fa fa-fw fa-floppy-o"></i></button>
                <button class="btn btn-outline-dark" id="btn-delete" data-toggle="tooltip" data-placement="bottom" title="Delete"><i class="fa fa-fw fa-trash-o"></i></button>
                <div class="btn-group" role="group">
                    <button class="btn btn-outline-dark dropdown-toggle" id="btn-fav" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" title="Favorites"><i class="fa fa-fw fa-star"></i></button>
                    <div class="dropdown-menu" aria-labelledby="btn-fav">
                        <a class="dropdown-item" id="btn-fav1" style="cursor: pointer;">Leds</a>
                        <a class="dropdown-item" id="btn-fav2" style="cursor: pointer;">7 Segments</a>
                        <a class="dropdown-item" id="btn-fav3" style="cursor: pointer;">Button</a>
                    </div>
                </div>
				<!--
					download:
					https://stackoverflow.com/questions/3665115/how-to-create-a-file-in-memory-for-user-to-download-but-not-through-server
				-->
            </div>
            <div class="btn-group mr-2" role="group">
                <!--
                <button class="btn btn-outline-dark" id="btn-upload" data-toggle="tooltip" data-placement="bottom" title="Upload binary"><i class="fa fa-fw fa-upload"></i></button>
                -->
                <%--
                https://stackoverflow.com/questions/5138719/change-default-text-in-input-type-file
                --%>
                <label class="btn btn-outline-dark" for="file-upload" id="btn-upload" data-toggle="tooltip" data-placement="bottom" title="Upload binary"><i class="fa fa-fw fa-upload"></i></label>
                <input type="file" name="file-upload" accept=".hex,.elf" id="file-upload" style="display: none"/>
                <button class="btn btn-outline-dark" id="start" data-toggle="tooltip" data-placement="bottom" title="Reset the Arduino"><i class="fa fa-fw fa-refresh"></i></button>
                <button class="btn btn-outline-dark" id="playpause" data-toggle="tooltip" data-placement="bottom" title="Run/Pause the Arduino"><i class="fa fa-fw fa-play"></i></button>
                <button class="btn btn-outline-dark" id="step" data-toggle="tooltip" data-placement="bottom" title="Step by step"><i class="fa fa-fw fa-step-forward"></i></button>
                <button class="btn btn-outline-dark" id="stop" data-toggle="tooltip" data-placement="bottom" title="Stop the Arduino"><i class="fa fa-fw fa-stop"></i></button>
            </div>
            <div class="btn-group mr-2" role="group">
                <button class="btn btn-outline-dark" id="btn-serial" data-toggle="tooltip" data-placement="bottom" title="Serial port"><i class="fa fa-fw fa-terminal"></i></button>
                <button class="btn btn-outline-dark" id="btn-flash" data-toggle="tooltip" data-placement="bottom" title="FLASH Memory"><i class="fa fa-fw fa-microchip"></i></button>
                <button class="btn btn-outline-dark" id="btn-sram" data-toggle="tooltip" data-placement="bottom" title="SRAM Memory"><i class="fa fa-fw fa-microchip"></i></button>
<!--                
                <button class="btn btn-outline-dark" id="btn-eeprom" data-toggle="tooltip" data-placement="bottom" title="EEPROM Memory"><i class="fa fa-fw fa-microchip"></i></button>
-->
                <button class="btn btn-outline-dark" id="btn-debug" data-toggle="tooltip" data-placement="bottom" title="Debug"><i class="fa fa-fw fa-bug"></i></button>
            </div>
            <div class="btn-group mr-2" role="group">
                <button class="btn btn-outline-dark" id="btn-zoom-out" data-toggle="tooltip" data-placement="bottom" title="Zoom out"><i class="fa fa-fw fa-search-minus"></i></button>
                <button class="btn btn-outline-dark" id="btn-zoom-reset" data-toggle="tooltip" data-placement="bottom" title="Reset the zoom"><i class="fa fa-fw fa-search"></i></button>
                <button class="btn btn-outline-dark" id="btn-zoom-in" data-toggle="tooltip" data-placement="bottom" title="Zoom in"><i class="fa fa-fw fa-search-plus"></i></button>
                <button class="btn btn-outline-dark" id="btn-undo" data-toggle="tooltip" data-placement="bottom" title="Undo"><i class="fa fa-fw fa-undo"></i></button>
                <button class="btn btn-outline-dark" id="btn-redo" data-toggle="tooltip" data-placement="bottom" title="Redo"><i class="fa fa-fw fa-repeat"></i></button>
            </div>
            <div class="btn-group mr-2" role="group">
                <button class="btn btn-outline-dark" id="btn-programmer" data-toggle="tooltip" data-placement="bottom" title="Connect to the Arduino IDE programmer"><i class="fa fa-fw fa-usb"></i></button>
                <button class="btn btn-outline-dark" id="btn-config" data-toggle="tooltip" data-placement="bottom" title="Config"><i class="fa fa-fw fa-cog"></i></button>
            </div>
            <div class="btn-group ml-auto" role="group">
                <div class="btn btn-outline-dark"><%=session.getAttribute("user")%></div>
                <button class="btn btn-outline-dark" id="btn-logout" data-toggle="tooltip" data-placement="bottom" title="Logout"><i class="fa fa-sign-out"></i></button>
            </div>
        </div>        



        <!-- main page -->

        <table id="info" border="0">
            <tr style="height: 40px;">
                <td style="min-width: 150px;">
                    <div>Project: <span id="projectName">-</span></div>
                </td>
                <td style="width: 20px;"></td>
                <td style="min-width: 150px;">
                    <div>Sketch: <span id="simulationName">-</span></div>
                </td>
                <td style="width: 20px;"></td>
                <td>
                    <div>Speed: <span id="speed">0.000</span> MHz</div>
                </td>
            </tr>
        </table>

        <div id="componentsPalette">
            <img data-shape="arduino.board.Uno" alt="Arduino Uno" src="arduinouno_24.png" class="draw2d_droppable component"><br>
            <img data-shape="arduino.LedVerde" alt="Green LED" src="leds-verde.png" class="draw2d_droppable component"><br>
            <img data-shape="arduino.LedVermelho" alt="Red LED" src="leds-vermelho.png" class="draw2d_droppable component"><br>
            <img data-shape="arduino.LedAzul" alt="Blue LED" src="leds-azul.png" class="draw2d_droppable component"><br>
            <img data-shape="arduino.Button" alt="Button" src="button.png" class="draw2d_droppable component"><br>
            <img data-shape="arduino.PushButton" alt="Push Button" src="push-button.png" class="draw2d_droppable component"><br>
            <img data-shape="arduino.SSDisplay" alt="7 Segments Display" src="seven-segments-display-layout.svg" class="draw2d_droppable component"><br>
            <img data-shape="arduino.Potentiometer" alt="Potentiometer" src="potentiometer_icon.png" class="draw2d_droppable component"><br>
        </div>

        <div id="draw2dCanvasWrapper">
            <div id="draw2dCanvas" 
                 class="canvas ui-droppable ui-widget-content" ></div>
        </div>


    </body>
</html>

