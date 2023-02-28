// Licensed under the Apache License, Version 2.0 (the "License"); you may not
// use this file except in compliance with the License. You may obtain a copy of
// the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
// License for the specific language governing permissions and limitations under
// the License.

var Nouveau = (function() {

  var index_results = []; // holds temporary emitted values during index

  function handleIndexError(err, doc) {
    if (err == "fatal_error") {
      throw(["error", "map_runtime_error", "function raised 'fatal_error'"]);
    } else if (err[0] == "fatal") {
      throw(err);
    }
    var message = "function raised exception " + err.toSource();
    if (doc) message += " with doc._id " + doc._id;
    log(message);
  };

  function assertType(name, expected, actual) {
    if (typeof actual !== expected) {
      throw({name: 'TypeError', message: 'type of ' + name + ' must be a ' + expected + ' not ' + typeof actual});
    }
  };

  return {
    index: function() {

      // Dreyfus compatibility.
      if (arguments.length == 2 || (arguments.length == 3 && typeof arguments[2] == 'object')) {

        var name = arguments[0];
        var value = arguments[1];
        var options = arguments[2] || {};

        assertType('name', 'string', name);

        if (name.substring(0, 1) === '_') {
          throw({name: 'ReservedName', message: 'name must not start with an underscore'});
        }

        if (typeof value == 'boolean') {
          // coerce to string as handling is the same.
          value = value ? 'true' : 'false'
        }

        if (!(typeof value == 'string' || typeof value == 'number')) {
          throw({name: 'TypeError', message: 'value must be a string, a number or boolean not ' + typeof value});
        }

        index_results.push({
          '@type': typeof value == 'string' ? 'string' : 'double',
          'name': name,
          'value': value,
          'store': options.store|| false,
          'facet': options.facet|| false
        });
      } else { // nouveau api
        var type = arguments[0];
        var name = arguments[1];

        assertType('type', 'string', type);
        assertType('name', 'string', name);

        if (name.substring(0, 1) === '_') {
          throw({name: 'ReservedName', message: 'name must not start with an underscore'});
        }

        switch (type) {
        case 'double':
        case 'string':
        case 'text':
          var value = arguments[2];
          var options = arguments[3] || {};
          assertType('value', type == 'double' ? 'number' : 'string', value);
          index_results.push({
            '@type': type,
            'name': name,
            'value': value,
            'store': options.store|| false,
            'facet': options.facet|| false,
            'sortable': options.sortable|| true
          });
          break;
        default:
          throw({name: 'TypeError', message: type + ' not supported'});
        }
      }
    },

    indexDoc: function(doc) {
      Couch.recursivelySeal(doc);
      var buf = [];
      for (var fun in State.funs) {
        index_results = [];
        try {
          State.funs[fun](doc);
          buf.push(index_results);
        } catch (err) {
          handleIndexError(err, doc);
          buf.push([]);
        }
      }
      print(JSON.stringify(buf));
    }

  }
})();
