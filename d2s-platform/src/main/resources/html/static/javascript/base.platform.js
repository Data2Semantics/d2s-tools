$(function() 
{
	// * Set up the tabs (basic jquery UI functionality)
	$(".js-tabs").tabs();
	
	// * Plot the graphs where required
	$('.graph').each(function ()
	{
		section = $(this);
		
		// * Generate an id and set up the inner container
		id = 'graph-' + randomString(12);
		section.append(
			$('<div>').addClass('plot').attr('id', id)
		);	
			
		// * Read the data from the raw data table
		dataSource = section.attr('data-source');	
		var data = load($('.' + dataSource));
		
		// * Store the data object in the section element in case the graph 
		//   needs to be redrawn.
		section.data('data', data);
		
		// -- Choose the default value for the input
		var defaultInput = null;
		$.each(data, function(name, list)
		{
			if(name.startsWith('i:'))
			{
				defaultInput = name;
				return false;
			}
		});
		
		if(defaultInput == null)
			defaultInput = 'index';
		
		plot(id, data, defaultInput, 'none', 1, 0);
		
		form = $('<form>').addClass('graph-form');
		section.append(form);
		
		left = $('<div>').addClass('left');
		right = $('<div>').addClass('right');
		form.append(left);
		form.append(right);
		
		// * selector for x axis
		field = $('<div>').addClass('field');
		left.append(field);
		field.append($('<label>').append('horizontal axis'));
		
		inputSelector = $('<select>').addClass('input-selector');
		field.append(inputSelector);
		
		$.each(data, function(name, list) 
		{
			option =  $('<option>')
			inputSelector.append(option);
			
			option.attr('value', name);
			if(name == defaultInput)
				option.attr('selected', 'selected');
			option.append(name);
			
		});
		
		inputSelector.change(graphUpdate);

		// * selector for color axis
		field = $('<div>').addClass('field');
		left.append(field);
		field.append($('<label>').append('color'));
		
		inputSelector = $('<select>').addClass('color-selector');
		field.append(inputSelector);
		
		$.each(data, function(name, list) 
		{
			option =  $('<option>')
			inputSelector.append(option);
			
			option.attr('value', name);
			option.append(name);
		});
		
		option =  $('<option>')
		inputSelector.append(option);
		
		option.attr('value', 'none');
		option.attr('selected', 'selected');
		option.append('none');
		
		inputSelector.change(graphUpdate);
		
		// * Slider for alpha
		slider = $('<div>').addClass('alpha-slider').slider({
			min: 0,
			max: 255,
			value: 255,
			change: graphUpdate
		});
		
		field = $('<div>').addClass('field');
		right.append(field);
		
		field.append($('<label>').append('alpha'));
		field.append(slider);
		
		
		// * Slider for jitter
		slider = $('<div>').addClass('jitter-slider').slider({
			min: 0,
			max: 255,
			value: 0,
			change: graphUpdate
		});
		
		field = $('<div>').addClass('field');
		right.append(field);
		field.append($('<label>').append('jitter'));
		field.append(slider);
		
		form.append($('<div>').addClass('clear'));
	});

	$(window).resize(function(){
		$('.graph-form').each(graphUpdate);
	});
});

function graphUpdate()
{	
	var section = $(this).parents('.graph');
	
	var id = section.find('.jqplot-target').attr('id'); 
	var data = section.data('data');
	
	var inputName = section.find('.graph-form .input-selector').val();
	var colorName = section.find('.graph-form .color-selector').val();
	
	var alpha = section.find('.graph-form .alpha-slider').slider('value') / 255.0;
	var jitter = section.find('.graph-form .jitter-slider').slider('value') / 255.0;
	
	plot(id, data, inputName, colorName, alpha, jitter);
}

/**
 * 
 * @param id A jQuery DOM element to which the graph should be drawn. 
 */
function plot(id, dataTable, inputName, colorName, alpha, jitter)
{	
	container = $('#'+id);
	
	// * Clear the graph if it has been drawn already.
	if(container.hasClass('activated'))
		container.empty();

	// * The div surrounding everything to do with this output
	var section = container.parents('.section');
	
	var outputName = section.find('.output-name').text().trim();
	
	var x = dataTable[inputName];
	var y = dataTable['output'];
	
	var data = [];
	var colors = [];
	
	for(i = 0; i < x.length; i++)
	{
		// * Create the tooltip text
		tip = $('<div>');
		table = $('<table>').addClass('tooltip-values');
		tip.append(table);
		

		$.each(dataTable, function(name, values) {
			if(name != 'index')
			{
				var tr = $('<tr>');
				tr.append($('<th>').append(name));
				tr.append($('<td>').append(values[i]));
				table.append(tr);
			}
		});
		
		// * add the data 
		data.push(
				[x[i] + (Math.random()-0.5) * jitter, 
				 y[i] + (Math.random()-0.5) * jitter, 
				 tip.html()]);
		
		if(colorName == 'none')
			colors.push(0.5);
		else
			colors.push(dataTable[colorName][i]);
	}
	
	colors = rescale(colors);
	
	colors = multiMap(colors, 1.0, 0.5, alpha);
	
	for(i = 0; i < colors.length; i++)
		console.log((colorName == 'none' ? 'none' : dataTable[colorName][i]) + ' ' + colors[i] + ' ' + data[i][0]);
			
	$.jqplot(id, [data],
	{
		axes: {
			xaxis: {
				pad: 0
			},
			yaxis: {
				pad: 0
			}
		},
		series: [{
			showMarker: true,
			showLine: false,
			fillAlpha: alpha,
			markerOptions: {size: 10.0},
        	renderer: $.jqplot.DifferentColorMarkerLineRenderer,
            rendererOptions: {
                fill: true,
                markerColors: colors,
                shapeRenderer:  $.jqplot.ShapeRenderer,
                shadowRenderer:  $.jqplot.ShadowRenderer
            }
		}],
	    axes:{
	          xaxis:{
	            label: inputName,
	            labelRenderer: $.jqplot.CanvasAxisLabelRenderer
	          },
	          yaxis:{
	            label: outputName,
	            labelRenderer: $.jqplot.CanvasAxisLabelRenderer
	          }
	        },
	    highlighter: {
	          show: true,
	          sizeAdjust: 7.5,
	          yvalues: 3,
	          formatString: '<!--%s %s --> %s'	          
	        },
	    cursor: {
	          show: true,
	          zoom: true
	        }
	});
	
	container.addClass('activated');
}


	
/**
 * Loads data from the given html table into a dictionary
 * 
 * All columns which contain all numbers are loaded.
 * 
 * @param html
 * 
 * @returns A dictionary of arrays of numbers
 */
function load(htmlTable)
{
	
	var data = {};
	var names = [];
	
	// * Add an array to data for each column name
	htmlTable.find('tr.names th').each(function(i, value)
		{
			th = $(this);
			
			if(th.hasClass('output'))
				name = "output";
			else
				name = "i:" + th.text().trim();
			
			data[name] = [];
			names[i] = name;
		});
	
	
	// * Load the values into the right columns
	htmlTable.find('tr').each(function(k, value) {
		
		tr = $(this);
		if(! tr.hasClass('names'))
			tr.find('td').each(function(i, value) 
			{
				td = $(this);
				value = td.text();
				
				if($.isNumeric(value))
					value = value - 0;
				else
					value = null;
				
				data[names[i]].push(value);
			});
	});	
	
	// * remove any columns which contain no numeric values
	$(names).each(function(i, name) {
		column = data[name];
		containsNumber = false;
		$(column).each(function(i, value)
		{
			if($.isNumeric(value))
				containsNumber = true;
		});
		
		if(! containsNumber)
			delete data[name];
	});
	
	// * Add index column
	n = data['output'].length;
	index = [];
	for(i = 0; i < n; i++)
		index[i] = i;
	
	data['index'] = index;
	
	return data;
}

/**
 * Produces a random string of the given length, to serve as a unique identifier
 * 
 * @param stringLength
 * @returns {String}
 */
function randomString(stringLength) 
{
	var chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
	var randomString = '';
	for (var i = 0; i < stringLength; i++) 
	{
		var rnum = Math.floor(Math.random() * chars.length);
		randomString += chars.substring(rnum,rnum+1);
	}
	return randomString
}

/**
 * Filter out data rows that contain zeroes 
 *
 * @param data
 * @returns
 */

function logFilter(data)
{
	result = [];
	$.each(data, function(index, row) {
		if(row.indexOf(0) == -1)
			result.push(row);
	});
	
	return result;
}


function rescale(input)
{
	var min = Number.MAX_VALUE;
	var max = Number.MIN_VALUE;
	
	$.each(input, function(i, value){
		min = Math.min(min, value);
		max = Math.max(max, value);
	})

	range = max - min;
	
	output = [];
	
	$.each(input, function(i, value)
	{
		output.push(range == 0.0 ? 212.0/255.0 : (value - min) / range);
	})
	
	return output;
}

function multiMap(inputs, saturation, lightness, alpha)
{
	colors = [];
	$.each(inputs, function(i, value) {
		colors.push(map(value, saturation, lightness, alpha));
	});
	
	return colors;
}

function map(input, saturation, lightness, alpha)
{
	color =  $.Color().hsla({ 
		hue: input * 255.0, 
		saturation: saturation, 
		lightness: lightness, 
		alpha: alpha
	})
	
	return color.toHslaString();
}