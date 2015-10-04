/* Rowlayout.js

	Purpose:
		
	Description:
		
	History:
		Thu, Jan 24, 2013  10:51:59 AM, Created by Neil

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

*/

var Rowlayout = 
zul.layout.Rowlayout = zk.$extends(zk.Widget, {
	// Logically, the container is divided into equal-width columns with spacing in between
	_ncols:    12,
	_spacing:  20,
	_colWidth: 60,
	
	$define: {
		ncols: [
			function(v) {
				return (v === undefined) ? 12 : (v < 1 ? 1 : v);
			},
			function() {
				if (this.desktop) this._fixLayout();
			}
		],
		
		spacing: [
			function(v) {
				return (v === undefined) ? 20 : (v < 0 ? 0 : v);
			},
			function() {
				if (this.desktop) this._fixLayout();
			}
		],
		
		colWidth: [
		    function(v) {
		    	return (v === undefined) ? 60 : (v < 0 ? 0 : v);
		    },
		    function() {	
				if (this.desktop) this._fixLayout();
		    }
		]
	},
	
	_fixLayout: function() {			
		for (var w = this.firstChild; w; w = w.nextSibling) {
			if (w.desktop) this._fixChild(w);
		}
	},
	
	_fixChild: function(child) {
		if (!child.desktop) return;
		
		var ncols     = this._ncols,
			spacing   = this._spacing, 
			colWidth  = this._colWidth,
			rowWidth  = ncols*colWidth + (ncols-1)*spacing;
			pSpacing  = spacing / rowWidth,
			pColWidth = colWidth / rowWidth;
		
		function colspanWidthRatio(col) {
			return pColWidth*col + pSpacing*(col-1);
		}
		function offsetMarginRatio(col) {
			return pColWidth*col + pSpacing*(col+1);
		}
		function percent(frac) {
			return (frac*100)+'%';
		}

		var n = child.$n(),
			offset = child._offset;
			marginLeft = (offset > 0) ? offsetMarginRatio(offset) : pSpacing;
		jq(n)
			.css('width', percent(colspanWidthRatio(child._colspan)))
			.css('margin-left', 
				(child == child.parent.firstChild)
				? percent(marginLeft - pSpacing) 
				: percent(marginLeft));
		
		// Responsive
		var matchMedia = window.matchMedia;
		if (matchMedia && matchMedia('(max-width: 767px)').matches) {
			jq(n)
				.css('width', '100%')
				.css('margin-left', 0);
		}
		
		// only fire when child has h/vflex
		for (var w = child.firstChild; w; w = w.nextSibling) {
			if (w._nvflex || w._nhflex) {
				zUtl.fireSized(w);
				break;
			}
		}
	},
	
	bind_: function() {
		this.$supers('bind_', arguments);
		zWatch.listen({onSize: this});
		
		this._fixLayout();
	},
	
	unbind_: function() {
		zWatch.unlisten({onSize: this});
		this.$supers('unbind_', arguments);
	},
	
	onSize: function() {
		this._fixLayout();
	}
});
