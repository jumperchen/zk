const path = require('path');
var webpack = require('webpack');
var CircularDependencyPlugin = require('circular-dependency-plugin');
var ForkTsCheckerWebpackPlugin = require('fork-ts-checker-webpack-plugin');

module.exports = {
	entry: {
		zk: {
			import: '/zk/src/main/resources/web/js/zk/index.ts',
			library: {
				type: 'window',
				export: 'default'
			}
		}
	},

	output: {
		filename: '[name].js',
		path: path.resolve(__dirname, 'build/output'),
	},

	resolve: {
		extensions: ['.ts', '.js', '.json'],
		alias: {
			'@zk': path.resolve('/zk/src/main/resources/web/js/zk')
		}
	},

	module: {
		rules: [{ test: /\.(ts|js)x?$/, loader: 'babel-loader', exclude: /node_modules/ }],
	},
	externals: {
		'jquery': 'jq', // for jquery.transit.js
		'moment': 'zk.mm', // for moment.js
	},

	plugins: [
		new ForkTsCheckerWebpackPlugin(),
		new webpack.IgnorePlugin({
			resourceRegExp: /^\.\/locale$/
		}),
		new CircularDependencyPlugin({
			failOnError: true
		})
	],

	devtool: 'source-map',
};
