
var arduino = {};

var componentProps = {
    NAME: "arduino.Component",

    layerShow: function (name, flag, duration) {
        if (this.svgNodes === null)
            return

        if (duration) {
            this.svgNodes.forEach(function (node) {
                if (node.data("name") === name) {
                    if (flag) {
                        node.attr({opacity: 0}).show().animate({opacity: 1}, duration)
                    } else {
                        node.animate({opacity: 0}, duration, function () {
                            this.hide()
                        })
                    }
                }
            })
        } else {
            this.svgNodes.forEach(function (node) {
                if (node.data("name") === name) {
                    if (flag) {
                        node.show()
                    } else {
                        node.hide()
                    }
                }
            })
        }
    },

    layerGet: function (name, attributes) {
        if (this.svgNodes === null)
            return null

        let result = null
        this.svgNodes.some(function (shape) {
            if (shape.data("name") === name) {
                result = shape
            }
            return result !== null
        })

        return result
    },

    layerAttr: function (name, attributes) {
        if (this.svgNodes === null)
            return

        this.svgNodes.forEach(function (shape) {
            if (shape.data("name") === name) {
                shape.attr(attributes)
            }
        })
    },

    getPersistentAttributes: function ()
    {
        var memento = {
            type: this.NAME,
            id: this.id,
            x: this.x,
            y: this.y,
        };

        return memento;
    },

    /**
     * Propaga o valor para as ligações ligadas a essa porta
     * 
     * @param {type} port
     */
    propagate: function (port, currentCon) {
        var cons = port.getConnections();
        var _this = this;

        cons.each(function (i, con) {
            if (con === currentCon) {
                return;
            }
            if (port.getValue() === null) {
                //rgb(18, 156, 228)
                con.setColor("#129CE4");
            } else if (port.getValue() === 0) {
                con.setColor("#000000");
            } else {
                con.setColor("#ff0000");
            }
            var other = con.getTarget();
            if (other === port) {
                other = con.getSource();
            }
            other.setValue(port.getValue());

            //isto permite propagar para portas com multiplas ligações
            other.getConnections().each(function (i, subcon) {
                if (subcon !== con) {
                    _this.propagate(other, con);
                }
            });


        });
    },

    propagateOnConnect: function (p1, p2) {
        var inPort = null;
        var outPort = null;

        var v1 = p1.getValue();
        var v2 = p2.getValue();

//        console.log("port1", v1);
//        console.log("port2", v2);

        if (v1 === null && v2 === null) {
            return null;
        }

        if (v1 !== null && v2 !== null) {
            if (v1 !== v2) {
                console.log('curto circuito!!!!!');
//                setTimeout(function () {
//                    alert('curto circuito!!!!!');
//                }, 500);
            }
            return null;
        }

        if (v1 !== null) {
            inPort = p1;
            outPort = p2;
        } else {
            inPort = p2;
            outPort = p1;
        }

        outPort.setValue(inPort.getValue());
        this.propagate(outPort);

        return outPort;
    },
    
    contextMenuItems: function() {
        return {};
    },

    contextMenuCallback: function(key, options) {
    },
    
    onContextMenu:function(x,y){
        $.contextMenu({
            selector: 'body', 
            events:
            {  
                hide:function(){ $.contextMenu( 'destroy' ); }
            },
            callback: $.proxy(function(key, options) 
            {
               switch(key){
               case "_toFront":
                   this.toFront();
                   break;
               case "_toBack":
                   this.toBack();
                   break;
               case "_blue":
                   this.setColor('#00A8F0');
                   break;
               case "_delete":
                   // with undo/redo support
                   var cmd = new draw2d.command.CommandDelete(this);
                   this.getCanvas().getCommandStack().execute(cmd);
               default:
                   this.contextMenuCallback(key, options);
                   break;
               }
            
            },this),
            x:x,
            y:y,
            items: $.extend($.extend(
            {
                "_toFront":    {name: "To Front", icon: "edit"},
                "_toBack":  {name: "To Back", icon: "cut"},
                //"blue":   {name: "Blue", icon: "copy"},
                "_sep1":   "---------",
            }, this.contextMenuItems()),{
                "_sep2":   "---------",
                "_delete": {name: "Delete", icon: "delete"}
            })
        });
   }

};
arduino.Component = draw2d.SetFigure.extend(componentProps);



arduino.Led = arduino.Component.extend({
    NAME: "arduino.Led",

    init: function (attr, setter, getter)
    {
        var _this = this;

        this._super($.extend({stroke: 0, bgColor: null, width: 60, height: 40}, attr), setter, getter);
        var port;
        // Port
        port = this.createPort("hybrid", new draw2d.layout.locator.XYRelPortLocator(102, 57));
        port.setConnectionDirection(1);
        port.setBackgroundColor("#37B1DE");
        port.setName("anode");  //+
        port.setMaxFanOut(1);
        port.setPersistPorts(false);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYRelPortLocator(-2, 57));
        port.setConnectionDirection(3);
        port.setBackgroundColor("#37B1DE");
        port.setName("cathode");    //-
        port.setMaxFanOut(1);
        port.setPersistPorts(false);

        this.setResizeable(false);
        this.installEditPolicy(new draw2d.policy.figure.AntSelectionFeedbackPolicy());

        this.value = 0;

        this.on("added", function () {
            _this.layerShow("low", true);
            _this.layerShow("high", false);
            //_this.getOutputPort(0).setValue(false);
        });

        this.getPorts().each(function (i, port) {
            port.on("connect", function (emitterPort, evt) {
                if (evt.connection.getTarget() && evt.connection.getSource()) {
                    _this.propagateOnConnect(evt.connection.getTarget(), evt.connection.getSource());
                }
            });
            port.on("disconnect", function (emitterPort, evt) {
                emitterPort.setValue(null);
            });
        });

    },

    getLedColor: function() {
        return "verde";
    },

    createShapeElement: function ()
    {
        var shape = this._super();
        this.originalWidth = 60;
        this.originalHeight = 40;
        return shape;
    },

    createSet: function ()
    {
        this.canvas.paper.setStart();
        var shape = null;
        // BoundingBox
        shape = this.canvas.paper.path("M0,0 L60,0 L60,40 L0,40");
        shape.attr({"stroke": "none", "stroke-width": 0, "fill": "none"});
        shape.data("name", "BoundingBox");

        // Label
//        shape = this.canvas.paper.ellipse();
//        shape.attr({"rx": 4.5, "ry": 4, "cx": 19.5, "cy": 23, "stroke": "none", "stroke-width": 0, "fill": "#000000", "dasharray": null, "opacity": 1});
//        shape.data("name", "Label");

        // Circle
//        shape = this.canvas.paper.ellipse();
//        shape.attr({"rx": 4.5, "ry": 4, "cx": 42.5, "cy": 23, "stroke": "none", "stroke-width": 0, "fill": "#000000", "dasharray": null, "opacity": 1});
//        shape.data("name", "Circle");

        // low
        shape = this.canvas.paper.image("leds-apagado.png", 19, 10, 24, 24);
        shape.data("name", "low");

        // high
        shape = this.canvas.paper.image("leds-"+this.getLedColor()+".png", 19, 10, 24, 24);
        shape.data("name", "high");

        // Line_shadow
        shape = this.canvas.paper.path('M45 23 L60,23');
        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "none", "stroke-width": 2, "stroke-dasharray": null, "opacity": 1});
        shape.data("name", "Line_shadow");

        // Line
        shape = this.canvas.paper.path('M45 23 L60,23');
        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "#000000", "stroke-width": 2, "stroke-dasharray": null, "opacity": 1});
        shape.data("name", "Line");

        // Line_shadow 2
        shape = this.canvas.paper.path('M0 23 L19.5,23');
        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "none", "stroke-width": 2, "stroke-dasharray": null, "opacity": 1});
        shape.data("name", "Line_shadow2");

        // Line 2
        shape = this.canvas.paper.path('M0 23 L19.5,23');
        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "#000000", "stroke-width": 2, "stroke-dasharray": null, "opacity": 1});
        shape.data("name", "Line2");


        shape = this.canvas.paper.path('M 43,23 L 47,23 M 45,21 L 45,25');
        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "#129CE4", "stroke-width": 2, "stroke-dasharray": null, "opacity": 1});
        shape.data("name", "mais");

        shape = this.canvas.paper.path('M 16,23 L 20,23');
        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "#129CE4", "stroke-width": 2, "stroke-dasharray": null, "opacity": 1});
        shape.data("name", "menos");

        return this.canvas.paper.setFinish();
    },

    onPortValueChanged: function (relatedPort) {
        //console.log(this.NAME+"."+relatedPort.getName()+": mudou valor", relatedPort.getValue());
        if (this.getPort("anode").getValue() > 0 && this.getPort("cathode").getValue() === 0) {
            this.layerShow("low", false);
            this.layerShow("high", true);
        } else {
            this.layerShow("low", true);
            this.layerShow("high", false);
        }
    },

});

arduino.LedVerde = arduino.Led.extend({
    NAME: "arduino.LedVerde",

    init: function (attr, setter, getter)
    {
        this._super(attr, setter, getter);
    },
});

arduino.LedVermelho = arduino.Led.extend({
    NAME: "arduino.LedVermelho",

    init: function (attr, setter, getter)
    {
        this._super(attr, setter, getter);
    },
    
    getLedColor: function() {
        return "vermelho";
    },

});

arduino.LedAzul = arduino.Led.extend({
    NAME: "arduino.LedAzul",

    init: function (attr, setter, getter)
    {
        this._super(attr, setter, getter);
    },
    
    getLedColor: function() {
        return "azul";
    },

});

arduino.SSDisplay = arduino.Component.extend({
    NAME: "arduino.SSDisplay",

    init: function (attr, setter, getter)
    {
        var _this = this;

        this.transparent = false;
        
        this._super($.extend({stroke: 0, bgColor: null, width: 135, height: 155}, attr), setter, getter);
        var port;

        // Port
        port = this.createPort("hybrid", new draw2d.layout.locator.XYRelPortLocator(-2, 11));
        port.setConnectionDirection(3);
        port.setBackgroundColor("#37B1DE");
        port.setName("a");  //+
        port.setMaxFanOut(1);
        port.setPersistPorts(false);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYRelPortLocator(-2, 22));
        port.setConnectionDirection(3);
        port.setBackgroundColor("#37B1DE");
        port.setName("b");  //+
        port.setMaxFanOut(1);
        port.setPersistPorts(false);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYRelPortLocator(-2, 33));
        port.setConnectionDirection(3);
        port.setBackgroundColor("#37B1DE");
        port.setName("c");  //+
        port.setMaxFanOut(1);
        port.setPersistPorts(false);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYRelPortLocator(-2, 44));
        port.setConnectionDirection(3);
        port.setBackgroundColor("#37B1DE");
        port.setName("d");  //+
        port.setMaxFanOut(1);
        port.setPersistPorts(false);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYRelPortLocator(-2, 55));
        port.setConnectionDirection(3);
        port.setBackgroundColor("#37B1DE");
        port.setName("e");  //+
        port.setMaxFanOut(1);
        port.setPersistPorts(false);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYRelPortLocator(-2, 66));
        port.setConnectionDirection(3);
        port.setBackgroundColor("#37B1DE");
        port.setName("f");  //+
        port.setMaxFanOut(1);
        port.setPersistPorts(false);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYRelPortLocator(-2, 77));
        port.setConnectionDirection(3);
        port.setBackgroundColor("#37B1DE");
        port.setName("g");  //+
        port.setMaxFanOut(1);
        port.setPersistPorts(false);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYRelPortLocator(-2, 88));
        port.setConnectionDirection(3);
        port.setBackgroundColor("#37B1DE");
        port.setName("dp");  //+
        port.setMaxFanOut(1);
        port.setPersistPorts(false);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYRelPortLocator(50, 102));
        port.setConnectionDirection(2);
        port.setBackgroundColor("#37B1DE");
        port.setName("cathode");    //-
        port.setMaxFanOut(1);
        port.setPersistPorts(false);


        this.setResizeable(true);
        this.setKeepAspectRatio(true);
        //this.installEditPolicy(new draw2d.policy.figure.AntSelectionFeedbackPolicy());

        this.on("added", function () {
            if( _this.transparent ) {
                _this.layerAttr("BoundingBox",{fill:"none"});
            } else {
                _this.layerAttr("BoundingBox",{fill:"#eeeeee"});
            }
        });

        this.getPorts().each(function (i, port) {
            port.on("connect", function (emitterPort, evt) {
                if (evt.connection.getTarget() && evt.connection.getSource()) {
                    _this.propagateOnConnect(evt.connection.getTarget(), evt.connection.getSource());
                }
            });
            port.on("disconnect", function (emitterPort, evt) {
                emitterPort.setValue(null);
            });
        });


        
    },

    createShapeElement: function ()
    {
        var shape = this._super();
        this.originalWidth = 188;
        this.originalHeight = 216;
        return shape;
    },

    createSet: function ()
    {
        this.canvas.paper.setStart();
        var shape = null;
        // BoundingBox
        shape = this.canvas.paper.path("M0,0 L 188,0 L 188,216 L0,216 z");
        shape.attr({"stroke": "#000000", "stroke-width": 1, "fill": "none"});
        shape.data("name", "BoundingBox");

        shape = this.canvas.paper.path('M 118.79731,26.000004 103.58534,41.500004 44.58535,41.500004 29.06756,26.000004 44.58535,10.500004 103.58534,10.500004 118.79731,26.000004 Z');
        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "#000000", "stroke-width": 2, "stroke-dasharray": null, "opacity": 1});
        shape.data("name", "a");

//        shape = this.canvas.paper.path('M 118.79731,26.000004 103.58534,41.500004 44.58535,41.500004 29.06756,26.000004 44.58535,10.500004 103.58534,10.500004 118.79731,26.000004 Z');
//        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "#000000", "fill": "#FF0000", "stroke-width": 2, "stroke-dasharray": null, "opacity": 1});
//        shape.data("name", "aa");

        shape = this.canvas.paper.path('M 118.79731,107.5 103.58534,123 44.58535,123 29.06756,107.5 44.58535,92.000004 103.58534,92.000004 118.79731,107.5 Z');
        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "#000000", "stroke-width": 2, "stroke-dasharray": null, "opacity": 1});
        shape.data("name", "g");

//        shape = this.canvas.paper.path('M 118.79731,107.5 103.58534,123 44.58535,123 29.06756,107.5 44.58535,92.000004 103.58534,92.000004 118.79731,107.5 Z');
//        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "#000000", "fill": "#FF0000", "stroke-width": 2, "stroke-dasharray": null, "opacity": 1});
//        shape.data("name", "gg");

        shape = this.canvas.paper.path('M 26,28.885134 41.5,44.097094 41.5,89.097084 26,104.61487 10.5,89.097084 10.5,44.097094 26,28.885134 Z');
        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "#000000", "stroke-width": 2, "stroke-dasharray": null, "opacity": 1});
        shape.data("name", "f");

//        shape = this.canvas.paper.path('M 26,28.885134 41.5,44.097094 41.5,89.097084 26,104.61487 10.5,89.097084 10.5,44.097094 26,28.885134 Z');
//        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "#000000", "fill": "#FF0000", "stroke-width": 2, "stroke-dasharray": null, "opacity": 1});
//        shape.data("name", "ff");

        shape = this.canvas.paper.path('M 122,28.885134 137.5,44.097094 137.5,89.097084 122,104.61487 106.5,89.097084 106.5,44.097094 122,28.885134 Z');
        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "#000000", "stroke-width": 2, "stroke-dasharray": null, "opacity": 1});
        shape.data("name", "b");

//        shape = this.canvas.paper.path('M 122,28.885134 137.5,44.097094 137.5,89.097084 122,104.61487 106.5,89.097084 106.5,44.097094 122,28.885134 Z');
//        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "#000000", "fill": "#FF0000", "stroke-width": 2, "stroke-dasharray": null, "opacity": 1});
//        shape.data("name", "bb");

        shape = this.canvas.paper.path('M 118.79731,189.55743 103.58534,205.05743 44.58535,205.05743 29.06756,189.55743 44.58535,174.05743 103.58534,174.05743 118.79731,189.55743 Z');
        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "#000000", "stroke-width": 2, "stroke-dasharray": null, "opacity": 1});
        shape.data("name", "d");

//        shape = this.canvas.paper.path('M 118.79731,189.55743 103.58534,205.05743 44.58535,205.05743 29.06756,189.55743 44.58535,174.05743 103.58534,174.05743 118.79731,189.55743 Z');
//        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "#000000", "fill": "#FF0000", "stroke-width": 2, "stroke-dasharray": null, "opacity": 1});
//        shape.data("name", "dd");

        shape = this.canvas.paper.path('M 26,110.94257 41.5,126.15453 41.5,171.15452 26,186.6723 10.5,171.15452 10.5,126.15453 26,110.94257 Z');
        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "#000000", "stroke-width": 2, "stroke-dasharray": null, "opacity": 1});
        shape.data("name", "e");

//        shape = this.canvas.paper.path('M 26,110.94257 41.5,126.15453 41.5,171.15452 26,186.6723 10.5,171.15452 10.5,126.15453 26,110.94257 Z');
//        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "#000000", "fill": "#FF0000", "stroke-width": 2, "stroke-dasharray": null, "opacity": 1});
//        shape.data("name", "ee");

        shape = this.canvas.paper.path('M 122,110.94257 137.5,126.15453 137.5,171.15452 122,186.6723 106.5,171.15452 106.5,126.15453 122,110.94257 Z');
        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "#000000", "stroke-width": 2, "stroke-dasharray": null, "opacity": 1});
        shape.data("name", "c");

//        shape = this.canvas.paper.path('M 122,110.94257 137.5,126.15453 137.5,171.15452 122,186.6723 106.5,171.15452 106.5,126.15453 122,110.94257 Z');
//        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "#000000", "fill": "#FF0000", "stroke-width": 2, "stroke-dasharray": null, "opacity": 1});
//        shape.data("name", "cc");

        shape = this.canvas.paper.ellipse();
        shape.attr({"rx": 18, "ry": 18, "cx": 159, "cy": 187, "stroke": "#000000", "stroke-width": 2, "fill": "none", "dasharray": null, "opacity": 1});
        shape.data("name", "dp");

//        shape = this.canvas.paper.ellipse();
//        shape.attr({"rx": 18, "ry": 18, "cx": 159, "cy": 187, "stroke": "#000000", "fill": "#FF0000", "stroke-width": 2, "fill": "none", "dasharray": null, "opacity": 1});
//        shape.data("name", "dpdp");

        return this.canvas.paper.setFinish();
    },

    onPortValueChanged: function (relatedPort) {
        //console.log(this.NAME+"."+relatedPort.getName()+": mudou valor", relatedPort.getValue());
        /*
        if (relatedPort.getValue() > 0 && this.getPort("cathode").getValue() === 0) {
            this.layerAttr(relatedPort.getName(),{fill:"#FF3C00"});
        } else {
            this.layerAttr(relatedPort.getName(),{fill:"none"});
        }
        */

        if( this.getPort("cathode")===relatedPort ) {
            if( this.getPort("cathode").getValue() === 0 ) {
                var str = "abcdefg";
                for (let i = 0; i < str.length; i++) {
                   this.layerAttr(str.charAt(i),{fill:"none"}); 
                }
                this.layerAttr("dp",{fill:"none"}); 
            }
        }
        if( this.getPort("cathode").getValue() === 0 ) {
            if (relatedPort.getValue() > 0) {
                this.layerAttr(relatedPort.getName(),{fill:"#FF3C00"});
            } else {
                this.layerAttr(relatedPort.getName(),{fill:"none"});
            }
        }
    },
    
    contextMenuItems: function() {
        return {
            "background": {name: "Background", icon: "edit"},
        };
    },

    contextMenuCallback: function(key, options) {
        switch (key) {
            case "background":
                this.transparent = !this.transparent;
                if( this.transparent ) {
                    this.layerAttr("BoundingBox",{fill:"none"});
                } else {
                    this.layerAttr("BoundingBox",{fill:"#eeeeee"});
                }
                break;
        }
    },

});


arduino.PushButton = arduino.Component.extend({
    NAME: "arduino.PushButton",

    init: function (attr, setter, getter)
    {
        var _this = this;
        this.dragging = false;
        
        this._super($.extend({stroke: 0, bgColor: null, width: 60, height: 27}, attr), setter, getter);
        var port;
        // Port
        port = this.createPort("hybrid", new draw2d.layout.locator.XYRelPortLocator(102, 84));
        port.setConnectionDirection(1);
        port.setBackgroundColor("#37B1DE");
        port.setName("p2");
        port.setMaxFanOut(1);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYRelPortLocator(-2, 84));
        port.setConnectionDirection(3);
        port.setBackgroundColor("#37B1DE");
        port.setName("p1");
        port.setMaxFanOut(1);

        this.setResizeable(false);
        this.installEditPolicy(new draw2d.policy.figure.AntSelectionFeedbackPolicy());

        this.value = false;
        this.inPort = null;
        this.outPort = null;
/*
        this.on("dragstart", function(o, a){
            console.log(o);
            console.log("DragStart ",a.x,a.y);
            //return o._super(a.x, a.y, a.shiftKey, a.ctrlKey);
            return false;
        });
*/        
        this.on("mousedown", function (arg1, arg2) {
            if( _this.dragging ) {
                return;
            }
            //o segundo parametro trás as coordenadas e pode ser usado para saber onde estamos a clicar
//            console.log(arg1, arg2);
//            console.log("press");
            _this.value = true;
            _this.layerShow("low", false, 100);
            _this.layerShow("high", true, 100);

            _this.outPort = _this.propagateOnConnect(_this.getPort("p1"), _this.getPort("p2"));
            if (_this.outPort !== null) {
                _this.inPort = _this.getPort("p1");
                if (_this.inPort === _this.outPort) {
                    _this.inPort = _this.getPort("p2");
                }
            }

        });
        this.on("mouseup", function () {
            if( _this.dragging ) {
                return;
            }
//            console.log("release");
            _this.value = false;
            _this.layerShow("low", true, 100);
            _this.layerShow("high", false, 100);
            if (_this.outPort !== null) {
                _this.outPort.setValue(null);
                _this.propagate(_this.outPort);
            }
        });

        this.on("added", function () {
            _this.layerShow("low", true);
            _this.layerShow("high", false);
        });

        this.getPorts().each(function (i, port) {
            port.on("connect", function (emitterPort, evt) {
                if (evt.connection.getTarget() && evt.connection.getSource()) {
                    _this.propagateOnConnect(evt.connection.getTarget(), evt.connection.getSource());
                }
            });
            port.on("disconnect", function (emitterPort, evt) {
                emitterPort.setValue(null);
            });
        });
    },

    onDragStart: function(x, y , shiftKey, ctrlKey) {
        if( (x>=15&&x<=48 && y>=8&&y<=15) 
                || (x>=24&&x<40 && y>=0&&y<=15) ) {
            return false;
        }
        if( x!==1 && y!==1 ) {
            this.dragging = true;
        }
        return this._super(x, y, shiftKey, ctrlKey);
    },
    
    onDragEnd: function(x, y , shiftKey, ctrlKey) {
        this.dragging = false;
    },
    
    createShapeElement: function ()
    {
        var shape = this._super();
        this.originalWidth = 60;
        this.originalHeight = 27;
        return shape;
    },

    createSet: function ()
    {
        this.canvas.paper.setStart();
        var shape = null;
        // BoundingBox
        shape = this.canvas.paper.path("M0,0 L60,0 L45,27 L0,27");
        shape.attr({"stroke": "none", "stroke-width": 0, "fill": "none"});
        shape.data("name", "BoundingBox");

        // Label
        shape = this.canvas.paper.ellipse();
        shape.attr({"rx": 4.5, "ry": 4, "cx": 19.5, "cy": 23, "stroke": "none", "stroke-width": 0, "fill": "#000000", "dasharray": null, "opacity": 1});
        shape.data("name", "Label");

        // Circle
        shape = this.canvas.paper.ellipse();
        shape.attr({"rx": 4.5, "ry": 4, "cx": 42.5, "cy": 23, "stroke": "none", "stroke-width": 0, "fill": "#000000", "dasharray": null, "opacity": 1});
        shape.data("name", "Circle");

        // low
        shape = this.canvas.paper.path('M41.125825000000077,8.735999999999876 Q39.125825000000077,8.735999999999876 39.125825000000077, 6.735999999999876 L39.125825000000077,2 Q39.125825000000077,0 37.125825000000077, 0 L26.125825000000077,0 Q24.125825000000077,0 24.125825000000077, 2 L24.125825000000077,6.735999999999876 Q24.125825000000077,8.735999999999876 22.125825000000077, 8.735999999999876 L17.2994250000001557,8.735999999999876 Q15.2994250000001557,8.735999999999876 15.2994250000001557, 10.735999999999876 L15.2994250000001557,11.735999999999876 Q15.2994250000001557,13.735999999999876 17.2994250000001557, 13.735999999999876 L45.299425000000156,13.735999999999876 Q47.299425000000156,13.735999999999876 47.299425000000156, 11.735999999999876 L47.299425000000156,10.735999999999876 Q47.299425000000156,8.735999999999876 45.299425000000156, 8.735999999999876 L41.125825000000077,8.735999999999876');
        shape.attr({"stroke": "#303030", "stroke-width": 1, "fill": "#FFFFFF", "dasharray": null, "opacity": 1});
        shape.data("name", "low");

        // high
        shape = this.canvas.paper.path('M37.31382500000018,16.235999999999876 Q35.50182500000028,16.235999999999876 36.26817190867477, 14.388647187036133 L38.359478091325588,9.347352812963743 Q39.125825000000077,7.5 37.125825000000077, 7.5 L26.125825000000077,7.5 Q24.125825000000077,7.5 24.62777239007231, 9.435987814424376 L25.888877609927716,14.3000121855755 Q26.39082499999995,16.235999999999876 24.39082499999995, 16.235999999999876 L17.2994250000001557,16.235999999999876 Q15.2994250000001557,16.235999999999876 15.2994250000001557, 18.235999999999876 L15.2994250000001557,19.235999999999876 Q15.2994250000001557,21.235999999999876 17.2994250000001557, 21.235999999999876 L45.299425000000156,21.235999999999876 Q47.299425000000156,21.235999999999876 47.299425000000156, 19.235999999999876 L47.299425000000156,18.235999999999876 Q47.299425000000156,16.235999999999876 45.299425000000156, 16.235999999999876 L41.125825000000077,16.235999999999876 Q39.125825000000077,16.235999999999876 37.31382500000018, 16.235999999999876 L37.31382500000018,16.235999999999876');
        shape.attr({"stroke": "#303030", "stroke-width": 1, "fill": "#FFFFFF", "dasharray": null, "opacity": 1});
        shape.data("name", "high");

        // Line_shadow
        shape = this.canvas.paper.path('M45 23 L60,23');
        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "none", "stroke-width": 2, "stroke-dasharray": null, "opacity": 1});
        shape.data("name", "Line_shadow");

        // Line
        shape = this.canvas.paper.path('M45 23 L60,23');
        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "#000000", "stroke-width": 2, "stroke-dasharray": null, "opacity": 1});
        shape.data("name", "Line");

        // Line_shadow 2
        shape = this.canvas.paper.path('M0 23 L19.5,23');
        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "none", "stroke-width": 2, "stroke-dasharray": null, "opacity": 1});
        shape.data("name", "Line_shadow2");

        // Line 2
        shape = this.canvas.paper.path('M0 23 L19.5,23');
        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "#000000", "stroke-width": 2, "stroke-dasharray": null, "opacity": 1});
        shape.data("name", "Line2");

        return this.canvas.paper.setFinish();
    },

//    createCommand: function (request) {
//        console.log(this.NAME+" createCommand",request);
//    },

    onPortValueChanged: function (relatedPort) {
//        console.log(this.NAME + "." + relatedPort.getName() + ": mudou valor", relatedPort.getValue());
        if (this.value === true) {
            if (this.inPort === null) {
                this.outPort = this.propagateOnConnect(this.getPort("p1"), this.getPort("p2"));
                if (this.outPort !== null) {
                    this.inPort = this.getPort("p1");
                    if (this.inPort === this.outPort) {
                        this.inPort = this.getPort("p2");
                    }
                }
            } else if (relatedPort === this.inPort) {
                this.outPort.setValue(this.inPort.getValue());
                this.propagate(this.outPort);
            }
        }
    },

});


arduino.Button = arduino.Component.extend({
    NAME: "arduino.Button",

    init: function (attr, setter, getter)
    {
        var _this = this;

        this._super($.extend({stroke: 0, bgColor: null, width: 60, height: 27}, attr), setter, getter);
        var port;
        // Port
        port = this.createPort("hybrid", new draw2d.layout.locator.XYRelPortLocator(102, 84));
        port.setConnectionDirection(1);
        port.setBackgroundColor("#37B1DE");
        port.setName("p2");
        port.setMaxFanOut(1);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYRelPortLocator(-2, 84));
        port.setConnectionDirection(3);
        port.setBackgroundColor("#37B1DE");
        port.setName("p1");
        port.setMaxFanOut(1);

        this.setResizeable(false);
        this.installEditPolicy(new draw2d.policy.figure.AntSelectionFeedbackPolicy());

        this.value = false;
        this.inPort = null;
        this.outPort = null;

        this.on("click", function () {
//            console.log("press");
            _this.value = !_this.value;
            
            _this.layerShow("low", !_this.value, 100);
            _this.layerShow("high", _this.value, 100);

            if( _this.value ) {
                _this.outPort = _this.propagateOnConnect(_this.getPort("p1"), _this.getPort("p2"));
                if (_this.outPort !== null) {
                    _this.inPort = _this.getPort("p1");
                    if (_this.inPort === _this.outPort) {
                        _this.inPort = _this.getPort("p2");
                    }
                }
            } else {
                if (_this.outPort !== null) {
                    _this.outPort.setValue(null);
                    _this.propagate(_this.outPort);
                }
            }
        });

        this.on("added", function () {
            _this.layerShow("low", !_this.value);
            _this.layerShow("high", _this.value);
        });

        this.getPorts().each(function (i, port) {
            port.on("connect", function (emitterPort, evt) {
                if (evt.connection.getTarget() && evt.connection.getSource()) {
                    _this.propagateOnConnect(evt.connection.getTarget(), evt.connection.getSource());
                }
            });
            port.on("disconnect", function (emitterPort, evt) {
                emitterPort.setValue(null);
            });
        });
    },

    createShapeElement: function ()
    {
        var shape = this._super();
        this.originalWidth = 60;
        this.originalHeight = 27;
        return shape;
    },

    createSet: function ()
    {
        this.canvas.paper.setStart();
        var shape = null;
        // BoundingBox
        shape = this.canvas.paper.path("M0,0 L60,0 L45,27 L0,27");
        shape.attr({"stroke": "none", "stroke-width": 0, "fill": "none"});
        shape.data("name", "BoundingBox");

        // Label
        shape = this.canvas.paper.ellipse();
        shape.attr({"rx": 4.5, "ry": 4, "cx": 19.5, "cy": 23, "stroke": "none", "stroke-width": 0, "fill": "#000000", "dasharray": null, "opacity": 1});
        shape.data("name", "Label");

        // Circle
        shape = this.canvas.paper.ellipse();
        shape.attr({"rx": 4.5, "ry": 4, "cx": 42.5, "cy": 23, "stroke": "none", "stroke-width": 0, "fill": "#000000", "dasharray": null, "opacity": 1});
        shape.data("name", "Circle");

        // low
        /*
        shape = this.canvas.paper.path('M41.125825000000077,8.735999999999876 Q39.125825000000077,8.735999999999876 39.125825000000077, 6.735999999999876 L39.125825000000077,2 Q39.125825000000077,0 37.125825000000077, 0 L26.125825000000077,0 Q24.125825000000077,0 24.125825000000077, 2 L24.125825000000077,6.735999999999876 Q24.125825000000077,8.735999999999876 22.125825000000077, 8.735999999999876 L17.2994250000001557,8.735999999999876 Q15.2994250000001557,8.735999999999876 15.2994250000001557, 10.735999999999876 L15.2994250000001557,11.735999999999876 Q15.2994250000001557,13.735999999999876 17.2994250000001557, 13.735999999999876 L45.299425000000156,13.735999999999876 Q47.299425000000156,13.735999999999876 47.299425000000156, 11.735999999999876 L47.299425000000156,10.735999999999876 Q47.299425000000156,8.735999999999876 45.299425000000156, 8.735999999999876 L41.125825000000077,8.735999999999876');
        shape.attr({"stroke": "#303030", "stroke-width": 1, "fill": "#FFFFFF", "dasharray": null, "opacity": 1});
        shape.data("name", "low");
        */
        shape = this.canvas.paper.path('M19.5,20 L40,9.5');
        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "#000000", "stroke-width": 3, "fill": "#FFFFFF", "dasharray": null, "opacity": 1});
        shape.data("name", "low");
       
        // high
        /*
        shape = this.canvas.paper.path('M37.31382500000018,16.235999999999876 Q35.50182500000028,16.235999999999876 36.26817190867477, 14.388647187036133 L38.359478091325588,9.347352812963743 Q39.125825000000077,7.5 37.125825000000077, 7.5 L26.125825000000077,7.5 Q24.125825000000077,7.5 24.62777239007231, 9.435987814424376 L25.888877609927716,14.3000121855755 Q26.39082499999995,16.235999999999876 24.39082499999995, 16.235999999999876 L17.2994250000001557,16.235999999999876 Q15.2994250000001557,16.235999999999876 15.2994250000001557, 18.235999999999876 L15.2994250000001557,19.235999999999876 Q15.2994250000001557,21.235999999999876 17.2994250000001557, 21.235999999999876 L45.299425000000156,21.235999999999876 Q47.299425000000156,21.235999999999876 47.299425000000156, 19.235999999999876 L47.299425000000156,18.235999999999876 Q47.299425000000156,16.235999999999876 45.299425000000156, 16.235999999999876 L41.125825000000077,16.235999999999876 Q39.125825000000077,16.235999999999876 37.31382500000018, 16.235999999999876 L37.31382500000018,16.235999999999876');
        shape.attr({"stroke": "#303030", "stroke-width": 1, "fill": "#FFFFFF", "dasharray": null, "opacity": 1});
        shape.data("name", "high");
        */
        shape = this.canvas.paper.path('M19.5,20 L42.5,20');
        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "#000000", "stroke-width": 3, "fill": "#FFFFFF", "dasharray": null, "opacity": 1});
        shape.data("name", "high");
       
       
        // Line_shadow
        shape = this.canvas.paper.path('M45 23 L60,23');
        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "none", "stroke-width": 2, "stroke-dasharray": null, "opacity": 1});
        shape.data("name", "Line_shadow");

        // Line
        shape = this.canvas.paper.path('M45 23 L60,23');
        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "#000000", "stroke-width": 2, "stroke-dasharray": null, "opacity": 1});
        shape.data("name", "Line");

        // Line_shadow 2
        shape = this.canvas.paper.path('M0 23 L19.5,23');
        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "none", "stroke-width": 2, "stroke-dasharray": null, "opacity": 1});
        shape.data("name", "Line_shadow2");

        // Line 2
        shape = this.canvas.paper.path('M0 23 L19.5,23');
        shape.attr({"stroke-linecap": "round", "stroke-linejoin": "round", "stroke": "#000000", "stroke-width": 2, "stroke-dasharray": null, "opacity": 1});
        shape.data("name", "Line2");

        return this.canvas.paper.setFinish();
    },

//    createCommand: function (request) {
//        console.log(this.NAME+" createCommand",request);
//    },

    onPortValueChanged: function (relatedPort) {
//        console.log(this.NAME + "." + relatedPort.getName() + ": mudou valor", relatedPort.getValue());
        if (this.value === true) {
            if (this.inPort === null) {
                this.outPort = this.propagateOnConnect(this.getPort("p1"), this.getPort("p2"));
                if (this.outPort !== null) {
                    this.inPort = this.getPort("p1");
                    if (this.inPort === this.outPort) {
                        this.inPort = this.getPort("p2");
                    }
                }
            } else if (relatedPort === this.inPort) {
                this.outPort.setValue(this.inPort.getValue());
                this.propagate(this.outPort);
            }
        }
    },

});


arduino.ComponentPot = draw2d.shape.widget.Slider.extend(componentProps);

//arduino.Potenciometer = draw2d.shape.widget.Slider.extend({
arduino.Potentiometer = arduino.ComponentPot.extend({
    NAME: "arduino.Potentiometer",
    
    init: function(attr, setter, getter) {
        var _this = this;
        this._super(attr, setter, getter);
        
        var port;
        // Port
        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(this.getWidth()+10, this.getHeight()/2));
        port.setConnectionDirection(1);
        port.setBackgroundColor("#37B1DE");
        port.setName("p1");
        port.setMaxFanOut(1);
        port.setPersistPorts(false);

        // Port
        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(-10, this.getHeight()/2));
        port.setConnectionDirection(3);
        port.setBackgroundColor("#37B1DE");
        port.setName("p2");
        port.setMaxFanOut(1);
        port.setPersistPorts(false);

        // Port
//        port = this.createPort("hybrid", new draw2d.layout.locator.XYRelPortLocator(50, -10));
        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(this.getWidth()/2, -10));
        port.setConnectionDirection(0);
        port.setBackgroundColor("#37B1DE");
        port.setName("out");  //-
        port.setMaxFanOut(1);
        port.setPersistPorts(false);
        
        this.setResizeable(false);
        this.installEditPolicy(new draw2d.policy.figure.AntSelectionFeedbackPolicy());
        
        this.getPorts().each(function (i, port) {
            port.on("connect", function (emitterPort, evt) {
                console.log(emitterPort);
                if (evt.connection.getTarget() && evt.connection.getSource()) {
                    _this.propagateOnConnect(evt.connection.getTarget(), evt.connection.getSource());
                }
            });
            port.on("disconnect", function (emitterPort, evt) {
                emitterPort.setValue(null);
            });
        });        
    },
    
    onPanning: function(dx, dy, dx2, dy2)
    {
        // calculate the current position of the mouse pos
        //
        var thumbW2 = this.slideBoundingBox.w/2;
        var width = this.getWidth();
//        console.log("width", width);
        var sliderWidth = width - this.padding.left - this.padding.right;
//        console.log("sliderWidth", sliderWidth);

        var figurePos = Math.min(width, Math.max(0,this.panningX+dx));
//        console.log("figurePos", figurePos);
        var sliderPos = Math.min(width-this.padding.left-this.padding.right,figurePos-this.padding.left)-thumbW2;
//        console.log("sliderPos", sliderPos);

//        this.setValue(100/sliderWidth*sliderPos);
        this.setValue(100/width*figurePos);
    },
    
    onValueChange: function(value) {
        let p1 = this.getPort("p1");
        let p2 = this.getPort("p2");
        let out = this.getPort("out");
        if( p1 && p2 && out ) {
            let v1 = p1.getValue();
            let v2 = p2.getValue();
//            console.log("in", v1, v2);
            if( v1!==null && v2!==null ) {
                let v = 0;
                if( v2>v1 ) {
                    v = v2-((v2-v1)*(value/100));
                } else {
                    v = v2+((v1-v2)*(value/100));
                }
                out.setValue(v);
//                console.log("pot value", v);
            } else {
                out.setValue(null);
//                console.log("pot value", null);
            }
            this.propagate(out);
        }
    },
});

arduino.board = {};
arduino.board.Uno = arduino.Component.extend({
    NAME: "arduino.board.Uno",

    //TODO: gerar um UUID se não for carregado de JSON
    //esse UUID será usado no backend para salvar a EEPROM
    init: function (attr, setter, getter)
    {
        var _this = this;

        this._super($.extend({stroke: 0, bgColor: null, width: 270, height: 381}, attr), setter, getter);
        var port;
        // Port
        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(257, 167));
        port.setConnectionDirection(1);
        port.setBackgroundColor("#000000");
        port.setColor("#FFFFFF");
        port.setName("gnd1");
        port.setMaxFanOut(20);
        port.setValue(0);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(257, 154));
        port.setConnectionDirection(1);
        port.setBackgroundColor("#37B1DE");
        port.setColor("#FF0000");
        port.setName("pin21");
        port.setMaxFanOut(20);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(257, 180));
        port.setConnectionDirection(1);
        port.setBackgroundColor("#37B1DE");
        port.setName("pin13");
        port.setMaxFanOut(20);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(257, 193));
        port.setConnectionDirection(1);
        port.setBackgroundColor("#37B1DE");
        port.setName("pin12");
        port.setMaxFanOut(20);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(257, 206));
        port.setConnectionDirection(1);
        port.setBackgroundColor("#37B1DE");
        port.setName("pin11");
        port.setMaxFanOut(20);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(257, 219));
        port.setConnectionDirection(1);
        port.setBackgroundColor("#37B1DE");
        port.setName("pin10");
        port.setMaxFanOut(20);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(257, 231));
        port.setConnectionDirection(1);
        port.setBackgroundColor("#37B1DE");
        port.setName("pin9");
        port.setMaxFanOut(20);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(257, 244));
        port.setConnectionDirection(1);
        port.setBackgroundColor("#37B1DE");
        port.setName("pin8");
        port.setMaxFanOut(20);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(257, 265));
        port.setConnectionDirection(1);
        port.setBackgroundColor("#37B1DE");
        port.setName("pin7");
        port.setMaxFanOut(20);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(257, 278));
        port.setConnectionDirection(1);
        port.setBackgroundColor("#37B1DE");
        port.setName("pin6");
        port.setMaxFanOut(20);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(257, 291));
        port.setConnectionDirection(1);
        port.setBackgroundColor("#37B1DE");
        port.setName("pin5");
        port.setMaxFanOut(20);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(257, 304));
        port.setConnectionDirection(1);
        port.setBackgroundColor("#37B1DE");
        port.setName("pin4");
        port.setMaxFanOut(20);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(257, 317));
        port.setConnectionDirection(1);
        port.setBackgroundColor("#37B1DE");
        port.setName("pin3");
        port.setMaxFanOut(20);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(257, 330));
        port.setConnectionDirection(1);
        port.setBackgroundColor("#37B1DE");
        port.setName("pin2");
        port.setMaxFanOut(20);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(257, 343));
        port.setConnectionDirection(1);
        port.setBackgroundColor("#37B1DE");
        port.setName("pin1");
        port.setMaxFanOut(20);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(257, 356));
        port.setConnectionDirection(1);
        port.setBackgroundColor("#37B1DE");
        port.setName("pin0");
        port.setMaxFanOut(20);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(13, 226));
        port.setConnectionDirection(3);
        port.setBackgroundColor("#FF0000");
        port.setName("pos1");
        port.setMaxFanOut(20);
        port.setValue(5);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(13, 213));
        port.setConnectionDirection(3);
        port.setBackgroundColor("#FF0000");
        port.setName("pos33");
        port.setMaxFanOut(20);
        port.setValue(3.3);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(13, 239));
        port.setConnectionDirection(3);
        port.setBackgroundColor("#000000");
        port.setColor("#FFFFFF");
        port.setName("gnd2");
        port.setMaxFanOut(20);
        port.setValue(0);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(13, 252));
        port.setConnectionDirection(3);
        port.setBackgroundColor("#000000");
        port.setColor("#FFFFFF");
        port.setName("gnd3");
        port.setMaxFanOut(20);
        port.setValue(0);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(13, 291));
        port.setConnectionDirection(3);
        port.setBackgroundColor("#37B1DE");
        port.setName("pin14");
        port.setMaxFanOut(20);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(13, 304));
        port.setConnectionDirection(3);
        port.setBackgroundColor("#37B1DE");
        port.setName("pin15");
        port.setMaxFanOut(20);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(13, 317));
        port.setConnectionDirection(3);
        port.setBackgroundColor("#37B1DE");
        port.setName("pin16");
        port.setMaxFanOut(20);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(13, 330));
        port.setConnectionDirection(3);
        port.setBackgroundColor("#37B1DE");
        port.setName("pin17");
        port.setMaxFanOut(20);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(13, 343));
        port.setConnectionDirection(3);
        port.setBackgroundColor("#37B1DE");
        port.setName("pin18");
        port.setMaxFanOut(20);

        port = this.createPort("hybrid", new draw2d.layout.locator.XYAbsPortLocator(13, 356));
        port.setConnectionDirection(3);
        port.setBackgroundColor("#37B1DE");
        port.setName("pin19");
        port.setMaxFanOut(20);


        this.setResizeable(false);
        this.installEditPolicy(new draw2d.policy.figure.AntSelectionFeedbackPolicy());

        this.on("added", function () {
            _this.layerShow("led13", false);
            _this.layerShow("ledPWR", false);
        });

        this.getPorts().each(function (i, port) {
            port.on("connect", function (emitterPort, evt) {
                if (evt.connection.getTarget() && evt.connection.getSource()) {
                    _this.propagateOnConnect(evt.connection.getTarget(), evt.connection.getSource());
                }
            });
        });

    },


    //https://stackoverflow.com/questions/105034/create-guid-uuid-in-javascript
    generateUUID: function() { // Public Domain/MIT
        var d = new Date().getTime();
        if (typeof performance !== 'undefined' && typeof performance.now === 'function'){
            d += performance.now(); //use high-precision timer if available
        }
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
            var r = (d + Math.random() * 16) % 16 | 0;
            d = Math.floor(d / 16);
            return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
        });
    },
            
    createShapeElement: function ()
    {
        var shape = this._super();
        this.originalWidth = 270;
        this.originalHeight = 381;
        return shape;
    },

    createSet: function ()
    {
        this.canvas.paper.setStart();
        var shape = null;
        // BoundingBox
        shape = this.canvas.paper.path("M0,0 L270,0 L270,381 L0,381");
        shape.attr({"stroke": "none", "stroke-width": 0, "fill": "none"});
        shape.data("name", "BoundingBox");

        shape = this.canvas.paper.image("arduino-uno-90.png", 0, 0, 270, 381);
        shape.attr({"stroke": "none", "stroke-width": 0, "fill": "none"});
        shape.data("name", "BoundingBox");

        shape = this.canvas.paper.rect(204, 171, 8, 7);
        shape.attr({"stroke": "none", "stroke-width": 0, "fill": "#FF0000"});
        shape.data("name", "led13");

        shape = this.canvas.paper.rect(180, 332, 8, 7);
        shape.attr({"stroke": "none", "stroke-width": 0, "fill": "#00FF00"});
        shape.data("name", "ledPWR");


        return this.canvas.paper.setFinish();
    },

    onPortValueChanged: function (relatedPort) {
//        console.log(this.NAME+"."+relatedPort.getName()+": mudou valor", relatedPort.getValue());
        if (relatedPort.getName().startsWith("pin")) {
            if (relatedPort.getValue()===null || relatedPort.getValue() === 0) {
                relatedPort.setColor("#000000");
            } else {
                relatedPort.setColor("#FF0000");
            }
        }

        if (relatedPort.getName() === "pin13") {
            //led 13 na placa
            this.layerShow("led13", relatedPort.getValue() > 0);
        }

        if (relatedPort.getName().startsWith("pin")) {
            let userData = relatedPort.getUserData();
            if( userData!==null && userData.arduino===true ) {
                this.propagate(relatedPort);
            } else {
                let pin = parseInt(relatedPort.getName().substring(3));
                this.onPinWrite(pin, relatedPort.getValue());
            }
        } else {
            this.propagate(relatedPort);
        }

    },

    setPower: function (value) {
        this.layerShow("ledPWR", value);
    },

    setPinValue(pin, value) {
        var port = this.getPort("pin"+pin);
        port.setUserData({'arduino': true});
        port.setValue(value);
        port.setUserData({'arduino': false});
    },
    
    getPinValue(pin) {
        var port = this.getPort("pin"+pin);
        return port.getValue();
    },
    
    onPinWrite: function(pin, value) {
        console.log("pinWrite", pin, value);
    }
});


