<%--
rowchildren.css.dsp

	Purpose:
		
	Description:
		
	History:
		Fri, Jan 25, 2013  6:49:13 PM, Created by Neil

Copyright (C) 2013 Potix Corporation. All Rights Reserved.

This program is distributed under GPL Version 3.0 in the hope that
it will be useful, but WITHOUT ANY WARRANTY.
--%>

.z-rowlayout > .z-rowchildren[class*="colspan"] {
  display: block;
  float: left;

  -webkit-box-sizing: border-box;
     -moz-box-sizing: border-box;
          box-sizing: border-box;
}

@media (max-width: 767px) {
  .z-rowlayout > .z-rowchildren[class*="colspan"] {
    float: none;
  }
}
