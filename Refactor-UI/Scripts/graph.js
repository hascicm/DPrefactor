    function init() {
      if (window.goSamples) goSamples();  // init for these samples -- you don't need to call this
      var $ = go.GraphObject.make;  // for conciseness in defining templates
      myDiagram =
        $(go.Diagram, "myDiagramDiv",  // Diagram refers to its DIV HTML element by id
        {
            // start everything in the middle of the viewport
            initialContentAlignment: go.Spot.Center,
            hasHorizontalScrollbar:false,
            hasVerticalScrollbar:false,
            maxSelectionCount:1,
            layout: $(go.TreeLayout,  // the layout for the entire diagram
            {
              angle: 90,
              arrangement: go.TreeLayout.ArrangementHorizontal,
              isRealtime: false,

            })
          });
      myDiagram.nodeTemplate =
      $(go.Node, "Auto",
        $(go.Shape, "Rectangle",
          { stroke: null, strokeWidth: 0 },
          new go.Binding("fill", "color")),
        $(go.TextBlock,
          { margin: 7, font: "Bold 14px Sans-Serif" },
            new go.Binding("text", "text")),
        {
          click: function(e, obj) { 
            console.log("Clicked " +  obj.data.desc);
            setDescriptionToTextarea(obj.data.desc);
            },
            selectionChanged: function(part) {
              var shape = part.elt(0);
              shape.fill = part.isSelected ? "red" : interpolateColor(part.data.weight);

            }
          }
          );
      myDiagram.linkTemplate =
      $(go.Link,
        { routing: go.Link.Orthogonal, corner: 10 },
        $(go.Shape, { strokeWidth: 2 }),
        $(go.Shape, { toArrow: "OpenTriangle" }),
        $(go.TextBlock, { 
          segmentOffset: new go.Point(NaN, NaN),
          segmentOrientation: go.Link.OrientUpright },
          new go.Binding("text", "text")),
        {
          click: function(e, obj) {
            setDescriptionToTextarea(obj.data.detail);
           console.log("Clicked " +  obj.data.detail); 
         }
       }
       );
      myDiagram.groupTemplate =
      $(go.Group, "Auto",
          { 
          layout: $(go.TreeLayout,
            { angle: 90, arrangement: go.TreeLayout.ArrangementHorizontal, isRealtime: false }),
            isSubGraphExpanded: true,
          },
          $(go.Shape, "Rectangle",
            { fill: "white", stroke: "gray", strokeWidth: 2 },
            new go.Binding("fill", "color")
            ),
          $(go.Panel, "Vertical",
            { defaultAlignment: go.Spot.Left, margin: 4 },
            $(go.Panel, "Horizontal",
              { defaultAlignment: go.Spot.Top },
              $(go.TextBlock,
                { font: "Bold 18px Sans-Serif", margin: 4 },
                new go.Binding("text", "text")
                )
              ),
            $(go.Placeholder,
              { padding: new go.Margin(0, 10) })
          )  
        );  

    }

    var stateCounter = 0;
    var edgeid = 0;
    var isStateDone = true;
    function addState(json){
      var groupkey = "group" + stateCounter;
      var groupnama;
      if (stateCounter == 0){
        groupname = "pôvodný stav"
      } else {
        groupname = "stav po " +stateCounter + ". oprave";
      }
      myDiagram.startTransaction("stateAdd"+stateCounter);

      // add state node 
      if (isStateDone || json.repair.isdone){
        myDiagram.model.addNodeData({key : groupkey, color:"green", text : groupname, isGroup : true})
      }else{
        myDiagram.model.addNodeData({key : groupkey, text : groupname, isGroup : true , color : "white"})
      }
      isStateDone = false;
      stateArray.push(groupkey);

      // add all smells 
      if (json.smells !=null){
        json.smells.forEach(function(smell) {
          var smellName = smell.name;
          var smellKey  = smell.refcode + stateCounter;
          var smellcolor = interpolateColor(smell.weight);
          let description = "";
          description += "názov pachu  : " + smell.name + "\n";
          description += "popis        : " + smell.description + "\n";
          description += "váha         : " + smell.weight + "\n";
          description += "kód pachu    : " + smell.refcode + "\n";
          description += "Poloha pachu\n";
          var x = 1;
          smell.position.forEach(function(value) {
            description += "Poloha  č." + x + "\n";
            description += "balík    :"  + value.package + "\n";
            description += "trieda   :" + value.class + "\n";
            if (value.method!= null)
              description += "metóda:   " + value.method + "\n";
            x++;

          })
          myDiagram.model.addNodeData({"key" : smellKey, "text": smellName, "color": smellcolor, "group":groupkey , "weight": smell.weight , desc : description});
        });
      }
      
      if (json.repair !=null){
        var fixedSmellKey = json.repair.code + (stateCounter -1 );
        let description = "";
        description += "názov orpavy : " + json.repair.repair + "\n";   
        if (json.repair.description!=null && !json.repair.description.includes("This is general repair for all pattern")){
          description += "popis        : " + json.repair.description + "\n";
        }
        description += "určené pre   : " + json.repair.code + "\n";
        // if (json.repair.isdone){
        //   isStateDone= true;
        // }
        //,text : json.repair.repair
        myDiagram.model.addLinkData({from:fixedSmellKey  , to:groupkey, id : edgeid , detail : description});
        edgeid++;

      }
      myDiagram.commitTransaction("stateAdd"+stateCounter);
      stateCounter++; 

    }


    var smellDinamicColor = true;
    var stateArray = [];

    function interpolateColor(smellWeight){
      if (smellDinamicColor){
        if (smellWeight < 1){
          smellWeight = 1
        }else if (smellWeight > 10){
          smellWeight = 10
        }

        smellWeight = (smellWeight - 1 ) * 10;
        return "hsl(" + smellWeight + " , 50% , 50%)"
      } else return "hsl(195, 24%, 50%)"
    }

    function getGraphData(clusterID, repairCount){
      myDiagram.clear();
      isStateDone = true;
      jQuery.get("http://localhost:8080/refactor/getGraphData/"+ clusterID + "/" + repairCount, function(response) {
        stateCounter = 0;
        stateArray = [];
        console.log(response);
        response.forEach(function(value) {  
          addState(value);
        })
      });
    }

    function editGroup(position,done){
      let key = stateArray[position-1];
      let data = myDiagram.model.findNodeDataForKey(key);
      myDiagram.model.removeNodeData(data);
      console.log("groupedit " + position + " " + done);
      if (done == true){
        data.color = "green";
      }else{
        data.color = "white"
      }
      myDiagram.model.addNodeData(data);


      console.log(data);
    }

    function setDescriptionToTextarea(text){
      $("#pathFinderResultGraphText").val(text);
    }

    function test(){
      myDiagram.addNodeData("");
    }