$(function() 
{
	// * Set up the tabs (basic jquery UI functionality
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
		
		plot(id, data, defaultInput, 'none');
		
		form = $('<form>').addClass('graph-form');
		section.append(form);
		
		// * selector for x axis
		form.append($('<label>').append('horizontal axis'));
		
		inputSelector = $('<select>').addClass('input-selector');
		form.append(inputSelector);
		
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
		form.append($('<label>').append('color'));
		
		inputSelector = $('<select>').addClass('color-selector');
		form.append(inputSelector);
		
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
	});

});

function graphUpdate()
{
	var section = $(this).parents('.graph');
	
	var id = section.find('.jqplot-target').attr('id'); 
	var data = section.data('data');
	
	var inputName = section.find('.graph-form .input-selector').val();
	var colorName = section.find('.graph-form .color-selector').val();
	
	plot(id, data, inputName, colorName);
}

/**
 * 
 * @param id A jQuery DOM element to which the graph should be drawn. 
 */
function plot(id, dataTable, inputName, colorName)
{
	container = $('#'+id);
	
	// * Clear the graph if it has been drawn already.
	if(container.hasClass('activated'))
		container.empty();

	// The div surrounding everything to do with this output
	var section = container.parents('.section');
	
	var outputName = section.find('.output-name').text().trim();
	
	var x = dataTable[inputName];
	var y = dataTable['output'];
	var z = dataTable[colorName];

	
	var data = [];
	for(i = 0; i < x.length; i++)
		data.push([x[i], y[i]]);
	
	console.log(data);
	
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
			markerOptions: {size: 10.0}
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