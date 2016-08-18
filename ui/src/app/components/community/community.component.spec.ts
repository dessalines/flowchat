/* tslint:disable:no-unused-variable */

import { By }           from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

import {
  beforeEach, beforeEachProviders,
  describe, xdescribe,
  expect, it, xit,
  async, inject
} from '@angular/core/testing';

import { CommunityComponent } from './community.component';

describe('Component: Community', () => {
  it('should create an instance', () => {
    let component = new CommunityComponent(null, null, null, null);
    expect(component).toBeTruthy();
  });
});
