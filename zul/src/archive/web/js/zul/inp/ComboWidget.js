/* ComboWidget.js

	Purpose:

	Description:

	History:
		Tue Mar 31 14:15:39     2009, Created by tomyeh

Copyright (C) 2009 Potix Corporation. All Rights Reserved.

This program is distributed under LGPL Version 2.1 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
*/
/**
 * A skeletal implementation for a combo widget.
 */
zul.inp.ComboWidget = zk.$extends(zul.inp.InputWidget, {
	_buttonVisible: true,
	_iconSclass: null,

	$define: {
		/** Returns whether the button (on the right of the textbox) is visible.
		 * <p>Default: true.
		 * @return boolean
		 */
		/** Sets whether the button (on the right of the textbox) is visible.
		 * @param boolean visible
	 	*/
		buttonVisible: function (v) {
			zul.inp.RoundUtl.buttonVisible(this, v);
		},
		/** Returns whether to automatically drop the list if users is changing
		 * this text box.
		 * <p>Default: false.
		 * @return boolean
		 */
		/** Sets whether to automatically drop the list if users is changing
		 * this text box.
		 * @param boolean autodrop
		 */
		autodrop: null,
		/** Returns the width of the popup of this component.
		 * @return String
		 * @since 8.0.3
		 */
		/**
		 * Sets the width of the popup of this component
		 * If the input is a percentage, the popup width will be calculated by multiplying the width of this component with the percentage.
		 * (e.g. if the input string is 130%, and the width of this component is 300px, the popup width will be 390px = 300px * 130%)
		 * Others will be set directly.
		 * @param String width of the popup of this component
		 * @since 8.0.3
		 */
		popupWidth: function (v) {
			if (this._open) {
				var pp = this.getPopupNode_(),
					pp2 = this.getPopupNode_(true);
				if (!pp) return;

				var ppofs = this._getPopupSize(pp, pp2);
				this._fixsz(ppofs);
				this._checkPopupSpaceAndPosition(pp, this.$n());
				this._fixFfWhileBothScrollbar(pp, pp2);
			}
		},
		/** Returns the type.
		 * <p>Default: text.
		 * @return String
		 */
		/** Sets the type.
		 * @param String type the type. Acceptable values are "text" and "password".
		 * Unlike XUL, "timed" is redudant because it is enabled as long as
		 * onChanging is added.
		 * @since 8.5.0
		 */
		type: zk.ie < 11 ? function () {
			this.rerender(); //though IE9 allows type to change but value is reset
		} : function (type) {
			var inp = this.getInputNode();
			if (inp)
				inp.type = type;
		},
		/**
		 * Returns the iconSclass name of this ComboWidget.
		 * @return String the iconSclass name
		 */
		/**
		 * Sets the iconSclass name of this ComboWidget.
		 * @param String iconSclass
		 * @since 8.6.2
		 */
		iconSclass: function (iconSclass) {
			var icon = this.$n('icon');
			if (this.desktop && icon)
				icon.className = (this.$s('icon') + ' ' + iconSclass);
		}
	},
	setWidth: function () {
		this.$supers('setWidth', arguments);
		if (this.desktop) {
			this.onSize();
		}
	},
	onSize: function () {
		zul.inp.RoundUtl.onSize(this);
		if (this._open) {
			var pp = this.getPopupNode_();
			if (pp)
				this._checkPopupSpaceAndPosition(pp, this.$n());
		}
	},

	onFloatUp: function (ctl) {
		if ((!this._inplace && !this.isOpen()) || jq(this.getPopupNode_()).is(':animated'))
			return;
		var wgt = ctl.origin;
		if (!zUtl.isAncestor(this, wgt)) {
			if (this.isOpen())
				this.close({sendOnOpen: true});
			if (this._inplace) {
				var n = this.$n(),
					inplace = this.getInplaceCSS();

				if (jq(n).hasClass(inplace)) return;

				n.style.width = jq.px0(zk(n).revisedWidth(n.offsetWidth));
				jq(this.getInputNode()).addClass(inplace);
				jq(n).addClass(inplace);
				this.onSize();
				n.style.width = this.getWidth() || '';
			}
		}
	},
	onResponse: function (ctl, opts) {
		if ((opts.rtags.onOpen || opts.rtags.onChanging) && this.isOpen()) {
			// ZK-2192: Only need to determine if popup is animating
			if (jq(this.getPopupNode_()).is(':animated')) {
				var self = this;
				setTimeout(function () {if (self.desktop) self.onResponse(ctl, opts);}, 50);
				return;
			}
			var pp = this.getPopupNode_(),
				pz = this.getPopupSize_(pp),
				scrollPos = {}; // Bug ZK-2294
			try {
				scrollPos.left = pp.scrollLeft;
				scrollPos.Top = pp.scrollTop;
				pp.style.height = 'auto'; // ZK-2086: BandBox popup invalid render if ON_OPEN event listener is attached

				// Bug 2941343, 2936095, and 3189142
				if (zk.ie8)
					pp.style.width = pz[0];
				this._fixsz(pz);
			} finally {
				// Bug ZK-2294, restore the scroll position
				pp.scrollTop = scrollPos.Top || 0;
				pp.scrollLeft = scrollPos.left || 0;
			}
		}
	},
	onScroll: function (wgt) {
		if (this.isOpen()) {
			// ZK-1552: fix the position of popup when scroll
			if (wgt) {
				var inp = this.getInputNode();
				// ZK-2211: should close when the input is out of view
				if (inp && zul.inp.InputWidget._isInView(this))
					zk(this.getPopupNode_()).position(inp, 'after_start');
				else
					this.close({sendOnOpen: true});
			}
		}
	},
	/** Drops down or closes the list of combo items ({@link Comboitem}.
	 * @param boolean open
	 * @param Map opts the options.
	 * @see #open
	 * @see #close
	 */
	setOpen: function (open, opts) {
		if (this.isRealVisible()) {
			if (open) this.open(opts);
			else this.close(opts);
		}
	},
	/** Returns whether the list of combo items is open
	 * @return boolean
	 */
	isOpen: function () {
		return this._open;
	},
	/** Drops down the list of combo items ({@link Comboitem}.
	 * It is the same as setOpen(true).
	 * @param Map opts the options.
	 */
	open: function (opts) {
		if (this._open) return;
		if (this._inplace) this._inplaceIgnore = true;
		this._open = true;
		if (opts && opts.focus)
			this.focus();

		var pp = this.getPopupNode_(),
			inp = this.getInputNode(),
			pp2 = this.getPopupNode_(true);
		if (!pp) return;

		this.setFloating_(true, {node: pp});
		zWatch.fire('onFloatUp', this); //notify all
		var topZIndex = this.setTopmost();

		var sclass = this.getSclass();
		pp.className = this.$s('popup') + (sclass ? ' ' + sclass : ''); // ZK-4234: updated sclass on combobox doesn't update popup

		pp.style.zIndex = topZIndex > 0 ? topZIndex : 1; //on-top of everything
		pp.style.position = 'absolute'; //just in case
		pp.style.display = 'block';

		var ppofs = this._getPopupSize(pp, pp2),
			ie10up = zk.ie10_ || zk.ie11_;
		if (ie10up) {
			// B70-ZK-2742: arrange method fixsz execution order
			this._fixsz(ppofs);//fix size
		}
		// throw out
		pp.style.visibility = 'hidden';
		pp.style.left = '-10000px';

		//FF: Bug 1486840
		//IE: Bug 1766244 (after specifying position:relative to grid/tree/listbox)
		//NOTE: since the parent/child relation is changed, new listitem
		//must be inserted into the popup (by use of uuid!child) rather
		//than invalidate!!
		var $pp = zk(pp);
		$pp.makeVParent();
		zWatch.fireDown('onVParent', this);

		if (ie10up)
			pp.style.height = pp.style.width = 'auto';
		this._fixsz(ppofs);

		// throw in
		pp.style.left = '';

		var n = this.$n(),
			jqn = jq(n), jqpp = jq(pp);

		this._checkPopupSpaceAndPosition(pp, n);
		this._shallSyncPopupPosition = false;

		var pptop = jqpp.offset().top;

		pp.style.display = 'none';
		pp.style.visibility = '';
		
		if (jqn.offset().top > pptop)
			this.slideDown_(pp, 'b');
		else
			this.slideDown_(pp);

		this._fixFfWhileBothScrollbar(pp, pp2);

		if (!this._shadow)
			this._shadow = new zk.eff.Shadow(pp,
				{left: -4, right: 4, top: -2, bottom: 3});

		if (opts && opts.sendOnOpen)
			this.fire('onOpen', {open: true, value: inp.value}, {rtags: {onOpen: 1}});

		//add extra CSS class for easy customize
		var openClass = this.$s('open');
		jqn.addClass(openClass);
		jqpp.addClass(openClass);
	},
	_getPopupSize: function (pp, pp2) {
		var ppofs = this.getPopupSize_(pp);
		pp.style.width = ppofs[0];
		pp.style.height = 'auto';

		if (pp2) pp2.style.width = pp2.style.height = 'auto';

		// B50-ZK-859: need to carry out min size here
		if (this.presize_())
			ppofs = this.getPopupSize_(pp);
		return ppofs;
	},
	_checkPopupSpaceAndPosition: function (pp, inp) {
		//B80-ZK-3051
		//check the popup space before position()
		var $pp = zk(pp),
			ppHeight = $pp.dimension().height,
			ppWidth = $pp.dimension().width,
			inpDim = (inp.nodeType ? zk(inp) : inp).dimension(true),
			inpTop = inpDim.top,
			inpLeft = inpDim.left,
			inpHeight = inpDim.height,
			inpWidth = inpDim.width,
			screenX = jq.innerX(),
			screenY = jq.innerY(),
			screenHeight = jq.innerHeight(),
			screenWidth = jq.innerWidth(),
			hPosition = 'start',
			vPosition = 'after',
			opts;

		if (screenX + screenWidth - inpLeft - inpWidth > ppWidth) {
			hPosition = 'start';
		} else if (inpLeft - screenX > ppWidth) {
			hPosition = 'end';
		} else {
			opts = {overflow: true};
		}
		if (screenY + screenHeight - inpTop - inpHeight > ppHeight) {
			vPosition = 'after';
		} else if (inpTop - screenY > ppHeight) {
			vPosition = 'before';
		} else {
			opts = {overflow: true};
		}

		$pp.position(inp, vPosition + '_' + hPosition, opts);
	},
	_fixFfWhileBothScrollbar: function (pp, pp2) {
		//FF issue:
		//If both horz and vert scrollbar are visible:
		//a row might be hidden by the horz bar.
		if (zk.gecko) {
			var rows = pp2 ? pp2.rows : null;
			if (rows) {
				var gap = pp.offsetHeight - pp.clientHeight;
				if (gap > 10 && pp.offsetHeight < 150) { //scrollbar
					var hgh = 0;
					for (var j = rows.length; j--;)
						hgh += rows[j].offsetHeight;
					pp.style.height = jq.px0((hgh + 20));
						//add the height of scrollbar (18 is an experimental number)
				}
			}
		}
	},
	_checkPopupPosition: function () {
		var pp = this.getPopupNode_(),
			$pp = zk(pp),
			inp = this.getInputNode(),
			ppDim = $pp.dimension(true),
			inpDim = zk(inp).dimension(true),
			ppBottom = ppDim.top + ppDim.height,
			ppRight = ppDim.left + ppDim.width,
			ppRelativeBottom = ppBottom - $pp.scrollOffset()[1], //minus scroll offset
			inpBottom = inpDim.top + inpDim.height,
			inpRight = inpDim.left + inpDim.width;

		if (ppRelativeBottom >= jq.innerHeight()
			|| (ppDim.top < inpDim.top && ppBottom < inpDim.top)
			|| ppDim.left < inpRight
			&& ppRight > inpDim.left
			&& ppBottom > inpDim.top
			&& ppDim.top < inpBottom
		) {
			return this._shallSyncPopupPosition = true;
		}
		return false;
	},
	/**
	 * Extra handling for min size of popup widget. Return true if size is affected.
	 */
	presize_: zk.$void,
	/** Slides down the drop-down list.
	 * <p>Default: <code>zk(pp).slideDown(this, {afterAnima: this._afterSlideDown});</code>
	 * @param DOMElement pp the DOM element of the drop-down list.
	 * @since 5.0.4
	 */
	slideDown_: function (pp, anchor) {
		zk(pp).slideDown(this, {afterAnima: this._afterSlideDown, duration: 100, anchor: anchor});
	},
	/** Slides up the drop-down list.
	 * <p>Default: <code>pp.style.display = "none";</code><br/>
	 * In other words, it just hides it without any animation effect.
	 * @param DOMElement pp the DOM element of the drop-down list.
	 * @since 5.0.4
	 */
	slideUp_: function (pp) {
		pp.style.display = 'none';
	},

	zsync: function () {
		this.$supers('zsync', arguments);
		if (!zk.css3 && this.isOpen() && this._shadow)
			this._shadow.sync();
	},
	_afterSlideDown: function (n) {
		if (!this.desktop) {
			//Bug 3035847: close (called by unbind) won't remove popup when animating
			zk(n).undoVParent(); //no need to fire onVParent since it will be removed
			jq(n).remove();
		}
		if (this._shadow) this._shadow.sync();
	},
	/** Returns the DOM element of the popup.
	 * Default: <code>inner ? this.$n("cave"): this.$n("pp")</code>.
	 * Override it if it is not the case.
	 * @param boolean inner whether to return the inner popup.
	 * ComboWidget assumes there is at least one popup and returned by
	 * <code>getPopupNode_()</code>, and there might be an inner DOM element
	 * returned by <code>getPopupNode_(true)</code>.
	 * @return DOMElement
	 * @since 5.0.4
	 */
	getPopupNode_: function (inner) {
		return inner ? this.$n('cave') : this.$n('pp');
	},

	/** Closes the list of combo items ({@link Comboitem} if it was
	 * dropped down.
	 * It is the same as setOpen(false).
	 * @param Map opts the options.
	 */
	close: function (opts) {
		if (!this._open) return;
		if (this._inplace) this._inplaceIgnore = false;
		var self = this;
		// ZK-2192: Only need to determine if popup is animating
		if (jq(this.getPopupNode_()).is(':animated')) {
			setTimeout(function () {if (self.desktop) self.close(opts);}, 50);
			return;
		}
		this._open = false;
		if (opts && opts.focus) {
			this.focus();
		}

		var pp = this.getPopupNode_();
		if (!pp) return;

		this.setFloating_(false);
		zWatch.fireDown('onHide', this);
		this.slideUp_(pp);

		zk.afterAnimate(function () {
			zk(pp).undoVParent();
			zWatch.fireDown('onVParent', self);
		}, -1);

		if (this._shadow) {
			this._shadow.destroy();
			this._shadow = null;
		}

		if (opts && opts.sendOnOpen)
			this.fire('onOpen', {open: false, value: this.getInputNode().value}, {rtags: {onOpen: 1}});

		//remove extra CSS class
		var openClass = this.$s('open');
		jq(this.$n()).removeClass(openClass);
		jq(pp).removeClass(openClass);
	},
	_fixsz: function (ppofs) {
		var pp = this.getPopupNode_();
		if (!pp) return;

		var pp2 = this.getPopupNode_(true);
		if (ppofs[1] == 'auto' && pp.offsetHeight > 350) {
			pp.style.height = '350px';
		} else if (pp.offsetHeight < 10) {
			// B65-ZK-2021: Only need to manually sync shadow when there is no item matched.
			if (this._shadow)
				this._shadow.sync();
		}

		var cb = this.$n(), i;
		if (i = this.getPopupWidth()) {
			if (i.endsWith('%')) {
				pp.style.width = jq.px0(cb.offsetWidth * parseFloat(i) / 100.0);
			} else {
				pp.style.width = i;
			}
			return;
		}

		if (ppofs[0] == 'auto') {
			if (pp.offsetWidth <= cb.offsetWidth) {
				pp.style.width = jq.px0(zk(pp).revisedWidth(cb.offsetWidth));
				if (pp2) pp2.style.width = '100%';
					//Note: we have to set width to auto and then 100%
					//Otherwise, the width is too wide in IE
			} else {
				var wd = jq.innerWidth() - 20;
				if (wd < cb.offsetWidth) wd = cb.offsetWidth;
				if (pp.offsetWidth > wd) pp.style.width = jq.px0(wd);
			}
		}
	},

	dnPressed_: zk.$void, //function (evt) {}
	upPressed_: zk.$void, //function (evt) {}
	otherPressed_: zk.$void, //function (evt) {}
	/** Called when the user presses enter when this widget has the focus ({@link #focus}).
	 * <p>call the close function
	 * @param zk.Event evt the widget event.
	 * The original DOM event and target can be retrieved by {@link zk.Event#domEvent} and {@link zk.Event#domTarget}
	 * @see #close
	 */
	enterPressed_: function (evt) {
		this.close({sendOnOpen: true});
		this.updateChange_();
		evt.stop();
	},
	/** Called when the user presses escape key when this widget has the focus ({@link #focus}).
	 * <p>call the close function
	 * @param zk.Event evt the widget event.
	 * The original DOM event and target can be retrieved by {@link zk.Event#domEvent} and {@link zk.Event#domTarget}
	 * @see #close
	 */
	escPressed_: function (evt) {
		this.close({sendOnOpen: true});
		evt.stop();
	},

	/** Returns [width, height] for the popup if specified by user.
	 * Default: ['auto', 'auto']
	 * @return Array
	 */
	getPopupSize_: function (pp) {
		return ['auto', 'auto'];
	},
	/** Called by {@link #redraw_} to redraw popup.
	 * <p>Default: does nothing
	 *  @param Array out an array of HTML fragments.
	 */
	redrawpp_: function (out) {
	},
	beforeParentMinFlex_: function (attr) { //'w' for width or 'h' for height
		if ('w' == attr)
			zul.inp.RoundUtl.syncWidth(this, this.$n('btn'));
	},
	afterKeyDown_: function (evt, simulated) {
		if (!simulated && this._inplace)
			jq(this.$n()).toggleClass(this.getInplaceCSS(), evt.keyCode == 13 ? null : false);

		return this.$supers('afterKeyDown_', arguments);
	},
	_dnInputOpen: function () {
		if (this._autodrop && !this._open)
			this.open({sendOnOpen: true});
	},
	bind_: function () {
		this.$supers(zul.inp.ComboWidget, 'bind_', arguments);
		var btn;

		if (btn = this.$n('btn')) {
			this.domListen_(btn, zk.android ? 'onTouchstart' : 'onClick', '_doBtnClick');
			if (this._inplace) this.domListen_(btn, 'onMouseDown', '_doBtnMouseDown');
		}

		this.domListen_(this.$n('real'), 'onInput', '_dnInputOpen');

		zWatch.listen({onSize: this, onFloatUp: this, onResponse: this, onScroll: this});
		if (!zk.css3) jq.onzsync(this);
	},
	unbind_: function () {
		this.close();

		var btn = this.$n('btn');
		if (btn) {
			this.domUnlisten_(btn, zk.android ? 'onTouchstart' : 'onClick', '_doBtnClick');
			if (this._inplace) this.domUnlisten_(btn, 'onMouseDown', '_doBtnMouseDown');
		}

		zWatch.unlisten({onSize: this, onFloatUp: this, onResponse: this, onScroll: this});
		if (!zk.css3) jq.unzsync(this);

		this.domUnlisten_(this.$n('real'), 'onInput', '_dnInputOpen');

		this.$supers(zul.inp.ComboWidget, 'unbind_', arguments);
	},
	inRoundedMold: function () {
		return true;
	},
	_doBtnClick: function (evt) {
		this._inplaceIgnore = false;
		if (!this._buttonVisible) return;
		// ZK-2192: Only need to determine if popup is animating
		if (!this._disabled && !jq(this.getPopupNode_()).is(':animated')) {
			if (this._open) this.close({focus: zul.inp.InputCtrl.isPreservedFocus(this),sendOnOpen: true});
			else this.open({focus: zul.inp.InputCtrl.isPreservedFocus(this),sendOnOpen: true});
		}
		if (zk.ios) { //Bug ZK-1313: keep window offset information before virtual keyboard opened on ipad
			this._windowX = window.pageXOffset;
			this._windowY = window.pageYOffset;
		}
		// Bug ZK-2544, B70-ZK-2849
		evt.stop((this._open ? {propagation: 1} : null));
	},
	_doBtnMouseDown: function (evt) {
		this._inplaceIgnore = true;
	},
	doKeyDown_: function (evt) {
		if (!this._disabled) {
			this._doKeyDown(evt);
			if (!evt.stopped)
				this.$supers('doKeyDown_', arguments);
		}
	},
	doClick_: function (evt) {
		if (!this._disabled) {
			if (evt.domTarget == this.getPopupNode_())
				this.close({
					focus: zul.inp.InputCtrl.isPreservedFocus(this),
					sendOnOpen: true
				});
			else if (this._readonly && !this.isOpen() && this._buttonVisible)
				this.open({
					focus: zul.inp.InputCtrl.isPreservedFocus(this),
					sendOnOpen: true
				});
			this.$supers('doClick_', arguments);
		}
	},
	_doKeyDown: function (evt) {
		var keyCode = evt.keyCode,
			bOpen = this._open;
			// Bug ZK-475, ZK-3635
		if (evt.target == this && (keyCode == 9 || (zk.webkit && keyCode == 0))) { //TAB or SHIFT-TAB (safari)
			if (bOpen)
				this.close({sendOnOpen: true});
			return;
		}

		if (evt.altKey && (keyCode == 38 || keyCode == 40)) {//UP/DN
			if (bOpen) this.close({sendOnOpen: true});
			else this.open({sendOnOpen: true});

			//FF: if we eat UP/DN, Alt+UP degenerate to Alt (select menubar)
			var opts = {propagation: true};
			if (zk.ie < 11) opts.dom = true;
			evt.stop(opts);
			return;
		}

		//Request 1537962: better responsive
		if (bOpen && (keyCode == 13 || keyCode == 27)) { //ENTER or ESC
			if (keyCode == 13) this.enterPressed_(evt);
			else this.escPressed_(evt);
			return;
		}

		if (keyCode == 38) this.upPressed_(evt);
		else if (keyCode == 40) this.dnPressed_(evt);
		else this.otherPressed_(evt);
	},
	/* B65-ZK-2021: Too many unnecessary shadow sync calls.
	onChildAdded_: _zkf = function (child) {
		if (this._shadow) this._shadow.sync();
	},
	onChildRemoved_: _zkf,
	onChildVisible_: _zkf,
	*/
	/** Utility to implement {@link #redraw}.
	 *  @param Array out an array of HTML fragments.
	 */
	redraw_: _zkf = function (out) {
		var uuid = this.uuid,
			isButtonVisible = this._buttonVisible;

		out.push('<span', this.domAttrs_({text: true, tabindex: true}), '><input id="',
			uuid, '-real" class="', this.$s('input'));

		if (!isButtonVisible)
			out.push(' ', this.$s('rightedge'));

		out.push('" autocomplete="off"',
			this.textAttrs_(), '/><a id="', uuid, '-btn" class="',
			this.$s('button'));

		if (!isButtonVisible)
			out.push(' ', this.$s('disabled'));

		out.push('"><i id="', uuid, '-icon" class="', this.$s('icon'), ' ', this.getIconSclass(),'"></i></a>');

		this.redrawpp_(out);

		out.push('</span>');
	}
}, {
	$redraw: _zkf
});
