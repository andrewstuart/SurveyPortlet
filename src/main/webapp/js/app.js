/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
window.up = window.up || {};

window.up.startSurveyPortlet = function(window, _, params) {
    'use strict';

    var surveyName = params.surveyName || '';
    var n = params.n;

    var MODULE_NAME = n + '-survey-portlet';
    var USER = params.user;

    if (!window.angular) {
        var ANGULAR_SCRIPT_ID = 'angular-uportal-script';

        var scr = document.getElementById(ANGULAR_SCRIPT_ID);

        if (!scr) {
            scr = document.createElement('script');
            scr.id =  ANGULAR_SCRIPT_ID;
            scr.type =  'text/javascript';
            scr.async =  true;
            scr.charset =  'utf-8';
            scr.src =  'https://cdnjs.cloudflare.com/ajax/libs/angular.js/1.3.15/angular.js';

            document.body.appendChild(scr);
        }

        scr.addEventListener('load', bootstrap);
    } else {
        register(window.up.ngApp);
    }

    function bootstrap() {
        var app = angular.module(MODULE_NAME, []);
        register(app);
        angular.bootstrap(document.getElementById(n + '-survey-portlet'), [MODULE_NAME]);
    }

    function register(app) {

app
.directive('modal', ["$document", function ($document) {
    return {
        template: '<ng-transclude></ng-transclude>',
        transclude: true,
        restrict: 'E',
        scope: {
            shown: '=',
            modalHide: '&?',
            modalShow: '&?'
        },
        link: function postLink($scope, iEle) {
            iEle.addClass('hidden');
            var modalHider = angular.element('<div class="modal-hider"></div>');

            var hasBeenShown;

            $scope.$watch('shown', function(shown) {
                if(shown) {
                    hasBeenShown = true;

                    iEle.removeClass('hidden');

                    modalHider.on('click', function() {
                        //Apparently $scope.$apply can't wrap the function passed to on()
                        $scope.$apply(function() {
                            $scope.shown = !$scope.shown;
                        });
                    });

                    angular.element(document.body).append(modalHider);

                    if($scope.modalShow) {
                        $scope.modalShow();
                    }
                } else {
                    iEle.addClass('hidden');

                    if(hasBeenShown && $scope.modalHide) {
                        $scope.modalHide();
                    }

                    modalHider.off('click');

                    if(modalHider) {
                        modalHider.remove();
                    }
                }
            });
        }
    };
}]);

app
.directive('ngDrag', ["DragData", function (DragData) {
    return {
        restrict: 'A',
        link: function postLink(scope, element, iAttrs) {
            element.attr('draggable', 'true');

            element.on('dragstart', function(e) {
                e.stopPropagation();
                e.originalEvent.dataTransfer.setData('ngdrag/type', iAttrs.ngDrag || 'ngdrag/id');
                e.originalEvent.dataTransfer.setData(iAttrs.ngDrag || 'ngdrag/id', scope.$id);
                DragData.add(scope);
            });
        }
    };
}])
.directive('ngDrop', ["DragData", function(DragData) {

    /**
     * @ngdoc directive
     * @name uni.directive:ngDrop
     * @param ngDrop The expression to evaluate upon drop. If dropped element came from ngDrag, the ngDrag $scope is
     * available as $from.
     * @description Allows an expression to be evaluated upon drop. Event available as $event.
     * @restrict A
     */
        return {
restrict: 'A',
link: function postLink($scope, element, iAttrs) {

    element.on('dragover', function(e) { e.preventDefault(); });

    element.on('dragenter', function(e) {
        e.preventDefault();
        e.stopPropagation();
        var type = e.originalEvent.dataTransfer.getData('ngdrag/type');
        if(type === iAttrs.allowDrop) {
            event.dataTransfer.dropEffect = 'move';
        } else {
            event.dataTransfer.dropEffect = 'none';
        }
    });

    element.on('drop', function(e) {
        e.preventDefault();

        var id = e.originalEvent.dataTransfer.getData(iAttrs.allowDrop || 'ngdrag/id');
        if(!id) { return; }
        var from = DragData.get(id);

        $scope.$apply(function() {
            $scope.$eval(iAttrs.ngDrop, {
                $from: from,
                $event: e
            });
        });
    });
}
};
}])
.service('DragData', function () {
    /**
     * @ngdoc service
     * @name uni.service:DragData
     * @description
     * # dragger service in the sabApp.  */
        var index = {};

    this.add = function(scope) {
        if(scope.$id) {
            index[scope.$id] = scope;
        }
    };

    this.get = function(id) {
        if(index[id]) {
            return index[id];
        }
    };
})

app
.service('StudentProfile', ["$http", "$q", "$timeout", function($http, $q, $timeout) {
    var sp = this;

    //var PROFILE_ROOT = 'https://portal-mock-api-dev.herokuapp.com/api/';
    var PROFILE_ROOT = '/survey-portlet/v1/surveys/';

    /**
     * @ngdoc
     * @methodOf cccPortal.service:StudentProfile
     * @name cccPortal.service:StudentProfile#get
     * @param {String} endpoint The endpoint/data type to query.
     * @param {Object} cfg Additional configuration for the request.
     * Most useful for params.
     * @description Get some data from the user profile.
     * @returns {Promise} A promise that will be resolved with the
     * value returned by the profile.
     */
    sp.get = function(endpoint, cfg) {
        var deferred = $q.defer();
        if (!endpoint) {
            $timeout(function() {
                deferred.reject('Missing endpoint for StudentProfile.get');
            });
        }
        cfg = cfg || {};

        $http(_.defaults({
            method: 'GET',
            url: PROFILE_ROOT + endpoint
        }, cfg)).success(deferred.resolve).error(deferred.reject);

        return deferred.promise;
    };

    sp.save = function(endpoint, data, cfg) {
        var deferred = $q.defer();

        if (!endpoint) {
            $timeout(function() {
                deferred.reject('Missing endpoint for StudentProfile.save');
            });
        }
        cfg = cfg || {};
        data = data || {};

        var verb = data.id ? 'PUT' : 'POST';
        var url = PROFILE_ROOT + endpoint + (data.id ? '/' + data.id : '');

        $http(_.defaults({
            method: verb,
            url: url,
            data: data
        }, cfg))
        .success(deferred.resolve)
        .error(deferred.reject);


        return deferred.promise;
    };
}]);

app.service('SurveyMeta', ["$http", "$filter", "$q", function($http, $filter, $q) {
    var sm = this;

    var currentId = -1;

    var root = '/survey-portlet/v1/surveys/';

    function newQuestion (q) {
        return $http({
            method: 'POST',
            data: q,
            url: root + 'questions/'
        });
    }

    function newSurveyQuestion (s, sq) {
        return newQuestion(sq.question).success(function(newQ) {
            _.extend(sq.question, newQ);

            return $http({
                method: 'POST',
                url: root + s.id + '/questions/' + newQ.id,
                data: sq
            }).success(function(newQ) {
                _.extend(sq, newQ);
            });
        });
    }

    /**
     * @ngdoc
     * @propertyOf cccPortal.service:SurveyMeta
     * @name cccPortal.service:SurveyMeta#surveysById
     * @description An index of surveys by their id
     */
    sm.surveysById = {};

    /**
     * @ngdoc
     * @propertyOf cccPortal.service:SurveyMeta
     * @name cccPortal.service:SurveyMeta#surveys
     * @description An array of surveys
     */
    sm.surveys = [];

    sm.surveys.clear = function() {
        while (this.length) {
            this.pop();
        }
    };

    /**
     * @ngdoc
     * @methodOf cccPortal.service:SurveyMeta
     * @name cccPortal.service:SurveyMeta#getSurveys
     * @description Retrieve the surveys from the metadata service and
     * refresh the sm.surveys array.
     * @returns {Promise} A promise that will be resolved when 
     */
    sm.getSurveys = function() {
        return $http.get(root)
        .success(function(surveys) {
            _.each(surveys, function(s) {
                if (sm.surveysById[s.id]) {
                    _.extend(sm.surveysById[s.id], s);
                } else {
                    sm.surveysById[s.id] = s;
                    sm.surveys.push(s);
                }

                //TODO clean up deleted surveys? (any not returned by $http.get)
            });

            return surveys;
        });
    };

    sm.getSurveyByName = function(name) {
        return $http.get(root + "surveyByName/" + name)
        .success(function(survey) {
            sm.surveysById[survey.id] = survey;
            sm.surveys.push(survey);
            return survey;
        });
    };

    /**
     * @ngdoc
     * @methodOf cccPortal.service:SurveyMeta
     * @name cccPortal.service:SurveyMeta#saveSurvey
     * @description Update the survey definition stored on the server.
     */
    sm.saveSurvey = function(survey) {
        var method = survey.id ? 'PUT' : 'POST';
        var url = survey.id ? root + survey.id : root ;

        _.extend(survey, {
            editable: undefined,
            lastUpdateDate: (new window.Date()).getTime()
        });

        survey = $http({
            method: method,
            url: url,
            data: survey})
        .success(function(survey) {
            sm.surveysById[survey.id] = survey;
            sm.surveys.push(survey);
            return survey;
        });

        survey = sm.getSurveyByName(survey.canonicalName);

        var requests = [];
        requests.push($http({
            method: method,
            url: url,
            data: survey,
        }));

        survey = angular.copy(survey);

        _.each(survey.surveyQuestions, function(q, i) {

            if ( q.question.id ){
                requests.push($http({
                    method: 'PUT',
                    url: root + 'questions/' + q.question.id,
                    data: q.question
                }));
            } else {
                requests.push(newSurveyQuestion(survey, q));
            }
        });

        return $q.all(requests);
    };

    var qaDef = {
        sequence: 0,
        canonicalName: null,
        answer: {
            text: null,
            imgUrl: null,
            helpText: null,
            altText: null,
            imgHeight: 0,
            imgWidth: 0
        },
        value: 0
    };

    /**
     * @ngdoc
     * @methodOf cccPortal.service:SurveyMeta
     * @name cccPortal.service:SurveyMeta#addQa
     * @param {Question} question A Question object that will have a
     * new answer.
     * @description Adds an empty answer to a given question
     */
    sm.addQa = function(question) {
        question.questionAnswers = question.questionAnswers || [];

        var newA = angular.copy(qaDef);
        newA.sequence = question.questionAnswers.length + 1;

        question.questionAnswers.push(newA);
    };

    var sqDef = {
        sequence: 1,
        question: {
            canonicalName: null,
            status: "UNPUBLISHED",
            text: null,
            altText: null,
            helpText: null,
        },
        numAllowedAnswers: 1
    };

    /**
     * @ngdoc
     * @methodOf cccPortal.service:SurveyMeta
     * @name cccPortal.service:SurveyMeta#addSq
     * @param {Object} survey The survey to add a question to.
     * @description Addsa a new survey question to a survey
     */
    sm.addSq = function(survey) {
        survey.surveyQuestions = survey.surveyQuestions || [];

        var newSq = angular.copy(sqDef);
        newSq.sequence = survey.surveyQuestions.length + 1;

        survey.surveyQuestions.push(newSq);
    };

    var surveyDef = {
        canonicalName: null,
        text: null,
        title: null,
        description: null,
        altText: null,
        helpText: null
    };

    /**
     * @ngdoc
     * @methodOf cccPortal.service:SurveyMeta
     * @name cccPortal.service:SurveyMeta#newSurvey
     * @description Returns a new survey object with appropriate keys.
     */
    sm.newSurvey = function() {
        return angular.copy(surveyDef);
    };
}]);

app
.controller('SurveyCtrl', ["$scope", "$filter", "SurveyMeta", "StudentProfile", function ($scope, $filter, SurveyMeta, StudentProfile) {
    /**
     * @ngdoc function
     * @name ngPortalApp.controller:SurveyCtrl
     * @description
     * # SurveyCtrl
     * Controller of the ngPortalApp
     */
    var survey;
    $scope.surveys = SurveyMeta.surveys;
    if (surveyName) {
        survey = SurveyMeta.getSurveyByName(surveyName);
    } else {
        SurveyMeta.getSurveys();
    }

    $scope.toggle = function(o) {
        o = o || {};
        o.shown = !o.shown;
    };


    StudentProfile.get('surveyAnswers', {
        params: {
            user: USER,
            survey: survey.id
        }
    }).then(function success(d, survey) {
        console.log(d);
        console.log(survey);
        if (d && d.length) {
            for (var i = 0; i < d.length; i++) {
                if (d[i].survey == survey.id) {
                    _.each(d[i].answers, function(ans) {
                        $scope.surveyData[ans.question] = ans.answer;
                    });
                    $scope.surveyData.id = d[i].id;
                }
            }
        }
    });

    $scope.saveAnswers = function(answers, survey) {
        delete answers.user;

        var data = {
            answers: _.chain(answers)
            .omit('id')
            .pairs()
            .map(function(e) {
                return {question: Number(e[0]), answer: e[1]};
            }).value(),
            id: answers.id,
            user: USER,
            survey: survey.id
        };
        StudentProfile.save('surveyAnswers', data)
        .then(function success(savedAnswers) {
            answers.id = savedAnswers.id;
        });
    };

    $scope.addQ = SurveyMeta.addSq;
    $scope.addA = SurveyMeta.addQa;

    $scope.save = SurveyMeta.saveSurvey;

    $scope.swapSeq = function(ele1, ele2) {
        var tmp = ele1.sequence;
        ele1.sequence = ele2.sequence;
        ele2.sequence = tmp;
    };

    $scope.surveyData = {};
}]);

app
.directive('surveyQuestion', function () {
    /**
     * @ngdoc directive
     * @name ngPortalApp.directive#SurveyQuestion
     * @description A directive to display a survey question.
     * @restrict E
     */
    return {
        template: '<section class="question">' +
            '<label class="text">{{def.question.text}}</label>' +
            '<div class="answer" ng-repeat="ans in def.question.questionAnswers | orderBy:\'sequence\'">' +
            '<label title="{{ans.answer.altText}}" aria-label="{{ans.answer.altText}}" >' +
            '<img ng-if="ans.answer.imgUrl" ng-src="{{ans.answer.imgUrl}}" height="25px" width="25px"></img>' +
            '<input ng-if="def.numAllowedAnswers === 1" type="radio" ng-model="survey[def.question.id]" ng-value="ans.answer.id" />' +
            '<input ng-if="def.numAllowedAnswers > 1" type="checkbox" ng-model="survey[def.question.id][ans.answer.id]"/>' +
            '{{ans.answer.text}}' +
            '</label>' +
            '<span class="glyphicon glyphicon-info-sign" ng-if="ans.answer.helpText" title="{{ans.answer.helpText}}"></span>' +
            '</div>' +
            '</section>',
        restrict: 'E',
        scope: {
            def: '=',
            survey: '=',
        },
        link: function postLink($scope, iEle, iAttrs) {
        }
    };
});

    }
};
