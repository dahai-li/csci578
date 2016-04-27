var ie = ( function() {
	var undef,
  v = 3,
  div = document.createElement('div'),
  all = div.getElementsByTagName('i');
  while (
    div.innerHTML='<!--[if gt IE ' + (++v) + ']><i></i><![endif]-->',all[0]
  );
  return v > 4 ? v : undef;
}() );

function takeAction() {
	if( ie && ie < 9 ) {
	 D3notok();
  } else {
    var head = document.getElementsByTagName('head')[0];
    var script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = "d3.min.js";
    script.addEventListener('load', D3ok, false);
    script.onload = "D3ok();";
    head.appendChild(script);
  }
}

function D3notok() {
  document.getElementById('sidepanel').style.visibility = 'hidden';
  var nocontent = document.getElementById('nocontent');
  nocontent.style.visibility = 'visible';
  nocontent.style.pointerEvents = 'all';
  var t = document.getElementsByTagName('body');
  var body = document.getElementsByTagName('body')[0];
  body.style.backgroundImage = "url('img/movie-network-screenshot-d.png')";
  body.style.backgroundRepeat = "no-repeat";
}

var selectComponent = undefined;

var toggleDiv = undefined;

var clearAndSelect = undefined;

var zoomCall = undefined;

function D3ok() {

  DEBUG = false;

  if( !window.console || !DEBUG ) {
    window.console = {};
    window.console.log = function () {};
  }

  var WIDTH = 960,
      HEIGHT = 600,
      SHOW_THRESHOLD = 2.5;

  var activeComponent = undefined;
  var currentOffset = { x : 0, y : 0 };
  var currentZoom = 1.0;

  var xScale = d3.scale.linear()
    .domain([0, WIDTH])
    .range([0, WIDTH]);
  var yScale = d3.scale.linear()
    .domain([0, HEIGHT])
    .range([0, HEIGHT]);
  var zoomScale = d3.scale.linear()
    .domain([1,6])
    .range([1,6])
    .clamp(true);

  var force = d3.layout.force()
    .charge(-320)
    .size( [WIDTH, HEIGHT] )
    .linkStrength( function(d,idx) { return d.weight; } );

  var svg = d3.select("#componentNetwork").append("svg:svg")
    .attr('xmlns','http://www.w3.org/2000/svg')
    .attr("width", WIDTH)
    .attr("height", HEIGHT)
    .attr("id","graph")
    .attr("viewBox", "0 0 " + WIDTH + " " + HEIGHT )
    .attr("preserveAspectRatio", "xMidYMid meet");

  componentInfoDiv = d3.select("#componentInfo");

  function getViewportSize( w ) {
    var w = w || window;
    console.log(w);
    if( w.innerWidth != null ) 
      return { w: w.innerWidth, 
	       h: w.innerHeight,
	       x : w.pageXOffset,
	       y : w.pageYOffset };
    var d = w.document;
    if( document.compatMode == "CSS1Compat" )
      return { w: d.documentElement.clientWidth,
	       h: d.documentElement.clientHeight,
	       x: d.documentElement.scrollLeft,
	       y: d.documentElement.scrollTop };
    else
      return { w: d.body.clientWidth, 
	       h: d.body.clientHeight,
	       x: d.body.scrollLeft,
	       y: d.body.scrollTop};
  }

  function getQStringParameterByName(name) {
    var match = RegExp('[?&]' + name + '=([^&]*)').exec(window.location.search);
    return match && decodeURIComponent(match[1].replace(/\+/g, ' '));
  }

  toggleDiv = function( id, status ) {
    d = d3.select('div#'+id);
    console.log( 'TOGGLE', id, d.attr('class'), '->', status );
    if( status === undefined )
      status = d.attr('class') == 'panel_on' ? 'off' : 'on';
      d.attr( 'class', 'panel_' + status );
      return false;
  }


  clearAndSelect = function (id) {
    toggleDiv('faq','off'); 
    toggleDiv('help','off'); 
    selectComponent(id,true);
  }


  function getComponentInfo( n, nodeArray ) {
    console.log( "INFO", n );
    var index = 0
    info = '<div id="cover">';
    if( n.name )
      info += '<div class=f><span class=l>APK name</span>: <span class=n>' 
           + n.name + '</span></div>';
    if( n.label )
      info += '<div class=f><span class=l>Component name</span>: <span class=c>' 
           + n.label + '</span></div>';
    if( n.type )
      info += '<div class=f><span class=l>Type</span>: <span class=t>' 
           + n.type + '</span></div>';
    if( n.description )
      info += '<div class=f><span class=l>Intent Description </span>: </br> <span class=d>' 
           + n.description + '</span></div>';
    return info;
  }

  d3.json(
    'homework3.json',
    function(data) {

    var nodeArray = data.nodes;
    var linkArray = data.links;
    console.log("NODES:",nodeArray);
    console.log("LINKS:",linkArray);

    minLinkWeight = 
      Math.min.apply( null, linkArray.map( function(n) {return n.weight;} ) );
    maxLinkWeight = 
      Math.max.apply( null, linkArray.map( function(n) {return n.weight;} ) );
    console.log( "link weight = ["+minLinkWeight+","+maxLinkWeight+"]" );

    force
      .nodes(nodeArray)
      .links(linkArray)
      .start();

    var node_size = d3.scale.linear()
      .domain([5,10])
      .range([1,16])
      .clamp(true);
    var edge_width = d3.scale.pow().exponent(8)
      .domain( [minLinkWeight,maxLinkWeight] )
      .range([1,3])
      .clamp(true);

    svg.call( d3.behavior.drag()
	      .on("drag",dragmove) );
    svg.call( d3.behavior.zoom()
	      .x(xScale)
	      .y(yScale)
	      .scaleExtent([1, 6])
	      .on("zoom", doZoom) );

    var networkGraph = svg.append('svg:g').attr('class','grpParent');

    var graphLinks = networkGraph.append('svg:g').attr('class','grp gLinks')
      .selectAll("line")
      .data(linkArray, function(d) {return d.source.id+'-'+d.target.id;} )
      .enter().append("line")
      .style('stroke-width', function(d) { return edge_width(d.weight);} )
      .attr("class", "link");

    var graphNodes = networkGraph.append('svg:g').attr('class','grp gNodes')
      .selectAll("circle")
      .data( nodeArray, function(d){ return d.id; } )
      .enter().append("svg:circle")
      .attr('id', function(d) { return "c" + d.index; } )
      .attr('class', function(d) { return 'node level'+d.level;} )
      .attr('r', function(d) { return node_size(d.score || 3); } )
      .attr('pointer-events', 'all')
      .on("click", function(d) { showComponentPanel(d); } )
      .on("mouseover", function(d) { highlightGraphNode(d,true,this);  } )
      .on("mouseout",  function(d) { highlightGraphNode(d,false,this); } );

    var graphLabels = networkGraph.append('svg:g').attr('class','grp gLabel')
      .selectAll("g.label")
      .data( nodeArray, function(d){return d.label} )
      .enter().append("svg:g")
      .attr('id', function(d) { return "l" + d.index; } )
      .attr('class','label');
   
    shadows = graphLabels.append('svg:text')
      .attr('x','-2em')
      .attr('y','-.3em')
      .attr('pointer-events', 'none')
      .attr('id', function(d) { return "lb" + d.index; } )
      .attr('class','nshadow')
      .text( function(d) { return d.label; } );

    labels = graphLabels.append('svg:text')
      .attr('x','-2em')
      .attr('y','-.3em')
      .attr('pointer-events', 'none')
      .attr('id', function(d) { return "lf" + d.index; } )
      .attr('class','nlabel')
      .text( function(d) { return d.label; } );

    function highlightGraphNode( node, on )
    {
      if( on && activeComponent !== undefined ) {
	     console.log("..clear: ",activeComponent);
	     highlightGraphNode( nodeArray[activeComponent], false );
	     console.log("..cleared: ",activeComponent);	
      }

      console.log("SHOWNODE "+node.index+" ["+node.label + "]: " + on);
      console.log(" ..object ["+node + "]: " + on);

      circle = d3.select( '#c' + node.index );
      label  = d3.select( '#l' + node.index );
      console.log(" ..DOM: ",label);

      console.log(" ..box CLASS BEFORE:", label.attr("class"));
      console.log(" ..circle",circle.attr('id'),"BEFORE:",circle.attr("class"));
      circle.classed( 'main', on );
      label.classed( 'on', on || currentZoom >= SHOW_THRESHOLD );
      label.selectAll('text').classed( 'main', on );
      console.log(" ..circle",circle.attr('id'),"AFTER:",circle.attr("class"));
      console.log(" ..box AFTER:",label.attr("class"));
      console.log(" ..label=",label);

      console.log(" ..SIBLINGS ["+on+"]: "+node.links);
      Object(node.links).forEach( function(id) {
	     d3.select("#c"+id).classed( 'sibling', on );
	     label = d3.select('#l'+id);
	     label.classed( 'on', on || currentZoom >= SHOW_THRESHOLD );
	     label.selectAll('text.nlabel').classed( 'sibling', on );
      } );

      activeComponent = on ? node.index : undefined;
      console.log("SHOWNODE finished: "+node.index+" = "+on );
    }

    selectComponent = function( new_idx, doMoveTo ) {
      console.log("SELECT", new_idx, doMoveTo );

      doMoveTo = doMoveTo || false;
      if( doMoveTo ) {
	     console.log("..POS: ", currentOffset.x, currentOffset.y, '->', nodeArray[new_idx].x, nodeArray[new_idx].y );
	     s = getViewportSize();
	     width  = s.w<WIDTH ? s.w : WIDTH;
	     height = s.h<HEIGHT ? s.h : HEIGHT;
	     offset = { x : s.x + width/2  - nodeArray[new_idx].x*currentZoom, y : s.y + height/2 - nodeArray[new_idx].y*currentZoom };
	     repositionGraph( offset, undefined, 'move' );
      }
      highlightGraphNode( nodeArray[new_idx], true );
      showComponentPanel( nodeArray[new_idx] );
    }

    function showComponentPanel( node ) {
      componentInfoDiv.html( getComponentInfo(node,nodeArray) ).attr("class","panel_on");
    }

    function repositionGraph( off, z, mode ) {
      console.log( "REPOS: off="+off, "zoom="+z, "mode="+mode );

      var doTr = (mode == 'move');

      if( off !== undefined && (off.x != currentOffset.x || off.y != currentOffset.y ) ) {
	      g = d3.select('g.grpParent')
	      if( doTr )
	       g = g.transition().duration(500);
	      g.attr("transform", function(d) { return "translate("+ off.x+","+off.y+")" } );
	      currentOffset.x = off.x;
	      currentOffset.y = off.y;
      }

      if( z === undefined ) {
	     if( mode != 'tick' )
	       return;
	     z = currentZoom;
      }else
	     currentZoom = z;

      e = doTr ? graphLinks.transition().duration(500) : graphLinks;
      e.attr("x1", function(d) { return z*(d.source.x); })
        .attr("y1", function(d) { return z*(d.source.y); })
        .attr("x2", function(d) { return z*(d.target.x); })
        .attr("y2", function(d) { return z*(d.target.y); });

      n = doTr ? graphNodes.transition().duration(500) : graphNodes;
      n.attr("transform", function(d) { return "translate("
					 +z*d.x+","+z*d.y+")" } );
      l = doTr ? graphLabels.transition().duration(500) : graphLabels;
      l.attr("transform", function(d) { return "translate("
					 +z*d.x+","+z*d.y+")" } );
    }
           
    function dragmove(d) {
      console.log("DRAG",d3.event);
      offset = { x : currentOffset.x + d3.event.dx,
		 y : currentOffset.y + d3.event.dy };
      repositionGraph( offset, undefined, 'drag' );
    }

    function doZoom( increment ) {
      newZoom = increment === undefined ? d3.event.scale 
					: zoomScale(currentZoom+increment);
      console.log("ZOOM",currentZoom,"->",newZoom,increment);
      if( currentZoom == newZoom )
	       return;

      if( currentZoom<SHOW_THRESHOLD && newZoom>=SHOW_THRESHOLD )
	       svg.selectAll("g.label").classed('on',true);
      else if( currentZoom>=SHOW_THRESHOLD && newZoom<SHOW_THRESHOLD )
	       svg.selectAll("g.label").classed('on',false);

      s = getViewportSize();
      width  = s.w<WIDTH  ? s.w : WIDTH;
      height = s.h<HEIGHT ? s.h : HEIGHT;

      zoomRatio = newZoom/currentZoom;
      newOffset = { x : currentOffset.x*zoomRatio + width/2*(1-zoomRatio),
		    y : currentOffset.y*zoomRatio + height/2*(1-zoomRatio) };
      console.log("offset",currentOffset,"->",newOffset);

      repositionGraph( newOffset, newZoom, "zoom" );
    }

    zoomCall = doZoom;

    force.on("tick", function() {
      repositionGraph(undefined,undefined,'tick');
    });

    mid = getQStringParameterByName('id')
    if( mid != null )
      clearAndSelect( mid );
  });
}
