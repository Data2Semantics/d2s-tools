$(function() {
	$(".js-tabs").tabs();
	
	
	$('.rs-line').each(function ()
	{
		section = $(this);
		
		id = 'rs-line-' + randomString(12);
		section.append(
			$('<div>').addClass('plot').attr('id', id)
		);	
			
		dataSource = section.attr('data-source');	
		useIndex = section.attr('data-use-index') ? 
				section.attr('data-use-index') == "true" : false;
		data = load($('.' + dataSource), useIndex);	
				
		
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
				showMarker: section.hasClass('scatter'),
				showLine: ! section.hasClass('scatter'),
				lineWidth: 2.5,
				markerOptions: {size: 3.0}
			}]
		});

		section.append(
				$('<div>')
					.addClass('log-plot')
					.attr('id', id+'-log')
		);	

		ldata = logFilter(data)
		filtered = data.length - ldata.length;
		
		$.jqplot(id+'-log', [ldata],
				{
					axes: {
						xaxis: {
							pad: 0,
							renderer: $.jqplot.LogAxisRenderer
						},
						yaxis: {
							pad: 0,
							renderer: $.jqplot.LogAxisRenderer
						}
					},
					series: [{
						showMarker: section.hasClass('scatter'),
						showLine: ! section.hasClass('scatter'),
						color:'#5FAB78',
						markerOptions: {size: 3.0}
					}]
				});	
		
		$('#'+id+'-log').addClass('hidden');
		
		section.prepend(
			$('<div>')
				.addClass('log-log')
				.append(
					$('<input>')
						.attr('type', 'checkbox')
						.attr('name', 'log-log')
						.click(loglogClicked)
				)
				.append(
					$('<label>')
						.append('log-log-axes')
				.append(
					$('<span>')
						.addClass('log-filter-message')
						.append('filtered out ' + filtered + ' data point(s) containing zero values')
						)
				)
		);
	});
	
	$('.rs-histogram').each(function () 
	{
		section = $(this);
		
		id = 'rs-histogram-' + randomString(12);
		section.append(
				$('<div>').addClass('plot').attr('id', id)
			);	
		
		dataSource = section.attr('data-source');
		useIndex = section.attr('data-use-index') ? 
				section.attr('data-use-index') == "true" : false;
		histoData = load($('.' + dataSource), useIndex);
		
	    $.jqplot(id, [histoData], {series:
	    			[{renderer:$.jqplot.BarRenderer}
	    		]});
		
	});

});

	
function loglogClicked()
{
	section = $(this).parent().parent();
	
	if($(this).is(':checked'))
	{
		section.addClass('log');
		section.find('.log-plot').removeClass('hidden');
		section.find('.plot').addClass('hidden');
	} else
	{
		section.removeClass('log');
		section.find('.plot').removeClass('hidden');
		section.find('.log-plot').addClass('hidden');		
	}
}
	
/**
 * Loads data from the given html table into an array
 * @param html
 */
function load(htmlTable, withIndex)
{
	data = [];
		
	htmlTable.find('tr').each(function(i, value) 
	{
		if($(value).find('td').size() > 0)
		{				
			dataRow = []
			if(withIndex)
				dataRow[0] = i;

			$(this).find('td').each(function(index, value)
			{
				dataRow[withIndex ? index + 1 : index] = parseFloat(
						$(value).html().replace(/,/g,''));
			})
			
			data.push(dataRow);
		}
	});
	
	return data;
}

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