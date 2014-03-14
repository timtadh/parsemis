#!/usr/bin/env ruby

require 'rubygems'
require 'buildr'
require 'rake'

repositories.remote << 'http://mirrors.ibiblio.org/pub/mirrors/maven2/'

parsemis_libs = [
  'antlr:antlr:jar:2.7.6',
  'org.prefuse:prefuse:jar:beta-20071021',
]

parsemis_layout = Layout.new
parsemis_layout[:source, :main, :java] = "src"

define 'parsemis', layout: parsemis_layout do
  project.version = 'git-master'
  compile.with parsemis_libs
  package(:jar).with(:manifest => {
    'Main-Class' => 'de.parsemis.Miner',
  }).merge(
    parsemis_libs
  )
end
