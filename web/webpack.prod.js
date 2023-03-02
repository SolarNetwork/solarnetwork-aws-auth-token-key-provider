const { merge } = require("webpack-merge");
const BaseConfig = require("./webpack.config.js");

const TerserPlugin = require("terser-webpack-plugin");

const devtool = "source-map";

module.exports = merge(BaseConfig, {
  mode: "production",
  plugins: [
    new TerserPlugin()
  ]
});
