/* calendar.js

{{IS_NOTE
	Purpose:

	Description:

	History:
		Fri June 9 10:29:16 TST 2009, Created by Flyworld
}}IS_NOTE

Copyright (C) 2008 Potix Corporation. All Rights Reserved.

{{IS_RIGHT
}}IS_RIGHT
*/
function (out) {
	var uuid = this.uuid,
		view = this._view,
		zcls = this.getZclass(),
		val = this.getTime(),
		m = val.getMonth(),
		d = val.getDate(),
		y = val.getFullYear(),
		ydelta = new zk.fmt.Calendar(val, this._localizedSymbols).getYear() - y, 
		yofs = y - (y % 10 + 1),
		ydec = zk.parseInt(y/100),
		tagnm = zk.ie || zk.gecko ? "a" : "button",
		localizedSymbols = this._localizedSymbols || {
			DOW_1ST: zk.DOW_1ST,
				ERA: zk.ERA,    
			 YDELTA: zk.YDELTA,
			   SDOW: zk.SDOW,
			  S2DOW: zk.S2DOW,
			   FDOW: zk.FDOW,
			   SMON: zk.SMON,
			  S2MON: zk.S2MON,
			   FMON: zk.FMON,
				APM: zk.APM
		},
		db = this.parent,
		numOfMonths = db ? db.getNumberOfMonths() : 1;
	
	out.push('<div ', this.domAttrs_(), '><table style="table-layout: fixed" width="', 215*numOfMonths, 'px"', zUtl.cellps0, '>',
			'<tr><td id="', uuid, '-tdl" class="', zcls, '-tdl');
	
	if (view == 'decade' && ydec*100 == 1900)
		out.push(' ', zcls, '-icon-disd');
		
	out.push('"><div class="', zcls, '-left"><div id="', uuid, '-ly" class="', zcls, '-left-icon"></div></div></td>');
	
	var isMultiMonth = numOfMonths > 1 && view == 'day';
	if (!isMultiMonth)
		out.push('<td><table class="', zcls, '-calctrl" width="100%" border="0" cellspacing="0" cellpadding="0">',
				'<tr><td id="', uuid, '-title" class="', zcls, '-title">');
	
	switch(view) {
	case "day" :
		if (numOfMonths > 1) {
			for (var i = 0; i < numOfMonths; ++i) {
				var curMon = m + i,
					isNext = curMon < 12,
					month = isNext ? curMon : curMon - 12,
					year = isNext ? y + ydelta : y + ydelta + 1;
				out.push('<td><table class="', zcls, '-calctrl" width="100%" border="0" cellspacing="0" cellpadding="0">',
						'<tr><td id="', uuid, '-title" class="', zcls, '-title">',
						'<span id="', uuid, '-tm" class="', zcls, '-ctrler">', localizedSymbols.SMON[month], '</span> <span id="', uuid, '-ty" class="', zcls, '-ctrler">', year, '</span>',
						'</td></tr></table></td>');
			}
		} else {
			out.push('<span id="', uuid, '-tm" class="', zcls, '-ctrler">', localizedSymbols.SMON[m], '</span> <span id="', uuid, '-ty" class="', zcls, '-ctrler">', y + ydelta, '</span>');
		}
		break;
	case "month" :
		out.push('<span id="', uuid, '-tm" class="', zcls, '-ctrler">', localizedSymbols.SMON[m], '</span> <span id="', uuid, '-ty" class="', zcls, '-ctrler">', y + ydelta, '</span>');
		break;
	case "year" :
		out.push('<span id="', uuid, '-tyd" class="', zcls, '-ctrler">', yofs + ydelta + 1, '-', yofs + ydelta + 10, '</span>');
		break;
	case "decade" :
		out.push('<span id="', uuid, '-tyd" class="', zcls, '-ctrler">', ydec*100 + ydelta, '-', ydec*100 + ydelta+ 99, '</span>');
		break;
	}
	out.push(isMultiMonth ? '' : '</td></tr></table></td>',
		'<td id="', uuid, '-tdr" class="', zcls, '-tdr');
		
	if (view == 'decade' && ydec*100 == 2000)
		out.push(' ', zcls, '-icon-disd');
	
	out.push('"><div class="', zcls, '-right"><div id="', uuid, '-ry" class="', zcls, '-right-icon"></div></div></td></tr>');
	//year view
	switch(view) {
	case "day" :
		if (numOfMonths > 1) {
			out.push('<tr>');
			for (var i = 0; i < numOfMonths; ++i) {
				out.push('<td colspan="', i == 0 || i == numOfMonths-1 ? 2 : 1,'" style="vertical-align: top; padding-right: 5px;"><table id="', uuid, '-mid', i,'" class="', zcls, '-calday" width="100%" border="0" cellspacing="0" cellpadding="0">',
						'<tr class="', zcls, '-caldow">');
					var sun = (7 - localizedSymbols.DOW_1ST) % 7, sat = (6 + sun) % 7;
					for (var j = 0 ; j < 7; ++j)
						out.push('<td class="', zcls, (j == sun || j == sat) ? '-wkend' : '-wkday', 
								'">' + localizedSymbols.S2DOW[j] + '</td>');
					out.push('</tr>');
					for (var j = 0; j < 6; ++j) { //at most 7 rows
						out.push('<tr class="', zcls, '-caldayrow" id="', uuid, '-w', i, j, '" >');
						for (var k = 0; k < 7; ++k)
							out.push ('<td class="', zcls, (k == sun || k == sat) ? '-wkend' : '-wkday', '"></td>');
						out.push('</tr>');
					}
				out.push('</table><', tagnm, ' id="', uuid, '-a', i,'" tabindex="-1" onclick="return false;" href="javascript:;" class="z-focus-a"></', tagnm,'></td>');
			}
		} else {
			out.push('<tr><td colspan="3"><table id="', uuid, '-mid" class="', zcls, '-calday" width="100%" border="0" cellspacing="0" cellpadding="0">',
					'<tr class="', zcls, '-caldow">');
				var sun = (7 - localizedSymbols.DOW_1ST) % 7, sat = (6 + sun) % 7;
				for (var j = 0 ; j < 7; ++j)
					out.push('<td class="', zcls, (j == sun || j == sat) ? '-wkend' : '-wkday', 
							'">' + localizedSymbols.S2DOW[j] + '</td>');
				out.push('</tr>');
				for (var j = 0; j < 6; ++j) { //at most 7 rows
					out.push('<tr class="', zcls, '-caldayrow" id="', uuid, '-w', j, '" >');
					for (var k = 0; k < 7; ++k)
						out.push ('<td class="', zcls, (k == sun || k == sat) ? '-wkend' : '-wkday', '"></td>');
					out.push('</tr>');
				}
		}
		break;
	case "month" :
		out.push('<tr><td colspan="3" ><table id="', uuid, '-mid" class="', zcls, '-calmon" width="100%" border="0" cellspacing="0" cellpadding="0">');
		for (var j = 0 ; j < 12; ++j) {
			if (!(j % 4)) out.push('<tr>');
			out.push('<td id="', uuid, '-m', j, '"_dt="', j ,'">', localizedSymbols.SMON[j] + '</td>');
			if (!((j + 1) % 4)) out.push('</tr>');
		}
		break;
	case "year" :
		out.push('<tr><td colspan="3" ><table id="', uuid, '-mid" class="', zcls, '-calyear" width="100%" border="0" cellspacing="0" cellpadding="0">');

		for (var j = 0 ; j < 12; ++j) {
			if (!(j % 4)) out.push('<tr>');
			out.push('<td _dt="', yofs ,'" id="', uuid, '-y', j, '" >', yofs + ydelta, '</td>');
			if (!((j + 1) % 4)) out.push('</tr>');
			yofs++;
		}
		break;
	case "decade" :
		out.push('<tr><td colspan="3" ><table id="', uuid, '-mid" class="', zcls, '-calyear" width="100%" border="0" cellspacing="0" cellpadding="0">');
		var temp = ydec*100 - 10;
		for (var j = 0 ; j < 12; ++j, temp += 10) {
			if (!(j % 4)) out.push('<tr>');
			if (temp < 1900 || temp > 2090) {
				out.push('<td>&nbsp;</td>');
				if (j + 1 == 12)
					out.push('</tr>'); 
				continue;
			}
			
			out.push('<td _dt="', temp ,'" id="', uuid, '-de', j, '" class="', (y >= temp && y <= (temp + 9)) ? zcls + '-seld' : '', '"',
					' >', temp + ydelta, '-<br />', temp + ydelta + 9, '</td>');
			if (!((j + 1) % 4)) out.push('</tr>');
		}
		break;
	}
	if (numOfMonths > 1) {
		out.push('</tr></table></div>');
	} else {
		out.push('</table><', tagnm, ' id="', uuid,
				'-a" tabindex="-1" onclick="return false;" href="javascript:;" class="z-focus-a"></',
				tagnm, '></td></tr></table></div>');
	}
}